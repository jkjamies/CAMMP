/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.domain.step.StepResult

import com.jkjamies.cammp.domain.model.DatasourceStrategy
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.MergeOutcome
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import java.nio.file.Files

class UpdateDataSourceDiStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val diRepo = mockk<DiModuleRepository>()
    val step = UpdateDataSourceDiStep(modulePkgRepo, diRepo)

    val tempRoot = Files.createTempDirectory("cammp_test_update_ds_di")
    val featureRoot = tempRoot.resolve("feature")
    val dataDir = featureRoot.resolve("data")
    val diDir = featureRoot.resolve("di")
    val dataSourceDir = featureRoot.resolve("dataSource")

    // Create physical directories so .exists() checks pass
    Files.createDirectories(dataDir)
    Files.createDirectories(diDir)
    Files.createDirectories(dataSourceDir)

    beforeContainer {
        clearAllMocks()
        // Mock package finding logic
        every { modulePkgRepo.findModulePackage(diDir) } returns "com.example.di"
        every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.data"
        every { modulePkgRepo.findModulePackage(dataSourceDir) } returns "com.example.datasource"
        // Mock the DI repo result
        coEvery { diRepo.mergeDataSourceModule(any(), any(), any(), any()) } returns MergeOutcome(diDir.resolve("DataSourceModule.kt"), "updated")
    }

    afterSpec {
        unmockkAll()
        tempRoot.toFile().deleteRecursively()
    }

    Given("an UpdateDataSourceDiStep with real file structure") {
        When("execute is called with Combined DataSource and Koin") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "User",
                datasourceStrategy = DatasourceStrategy.Combined,
                diStrategy = DiStrategy.Koin(useAnnotations = false)
            )

            val result = step.execute(params)

            Then("it should call mergeDataSourceModule with correct Koin bindings") {
                result.shouldBeInstanceOf<StepResult.Success>()

                val slot = slot<List<DataSourceBinding>>()
                coVerify {
                    diRepo.mergeDataSourceModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        desiredBindings = capture(slot),
                        useKoin = true
                    )
                }

                val bindings = slot.captured
                bindings.shouldHaveSize(1)
                // Verify imports and signature
                bindings[0].ifaceImport shouldBe "import com.example.data.dataSource.UserDataSource"
                bindings[0].implImport shouldBe "import com.example.datasource.UserDataSourceImpl"
                bindings[0].signature shouldContain "single<UserDataSource> { UserDataSourceImpl(get()) }"
            }
        }

        When("execute is called with Combined DataSource and Hilt") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "User",
                datasourceStrategy = DatasourceStrategy.Combined,
                diStrategy = DiStrategy.Hilt
            )

            val result = step.execute(params)

            Then("it should call mergeDataSourceModule with correct Hilt bindings") {
                result.shouldBeInstanceOf<StepResult.Success>()

                val slot = slot<List<DataSourceBinding>>()
                coVerify {
                    diRepo.mergeDataSourceModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        desiredBindings = capture(slot),
                        useKoin = false
                    )
                }

                val bindings = slot.captured
                bindings.shouldHaveSize(1)
                bindings[0].signature shouldContain "abstract fun bindUserDataSource(impl: UserDataSourceImpl): UserDataSource"
            }
        }
    }
})
