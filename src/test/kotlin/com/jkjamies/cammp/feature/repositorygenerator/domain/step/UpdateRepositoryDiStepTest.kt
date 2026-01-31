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

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.MergeOutcome
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Paths

/**
 * Tests for [UpdateRepositoryDiStep].
 */
class UpdateRepositoryDiStepTest : BehaviorSpec({

    Given("UpdateRepositoryDiStep") {
        val root = Paths.get("/project/feature")
        val dataDir = root.resolve("data")
        val domainDir = root.resolve("domain")
        val diDir = root.resolve("di")

        fun params(di: DiStrategy) = RepositoryParams(
            dataDir = dataDir,
            className = "MyRepository",
            datasourceStrategy = DatasourceStrategy.None,
            diStrategy = di,
        )

        When("execute is called with Hilt strategy") {
            Then("it should merge repository module with useKoin=false") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val diRepo = mockk<DiModuleRepository>()
                val step = UpdateRepositoryDiStep(modulePkgRepo, diRepo)

                every { modulePkgRepo.findModulePackage(diDir) } returns "com.example.di"
                every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.data"
                every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.domain"

                coEvery {
                    diRepo.mergeRepositoryModule(any(), any(), any(), any(), any(), any())
                } returns MergeOutcome(diDir.resolve("RepositoryModule.kt"), "Updated")

                step.execute(params(DiStrategy.Hilt)).shouldBeInstanceOf<StepResult.Success>()

                coVerify(exactly = 1) {
                    diRepo.mergeRepositoryModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        className = "MyRepository",
                        domainFqn = "com.example.domain.repository",
                        dataFqn = "com.example.data.repository",
                        useKoin = false,
                    )
                }
            }
        }

        When("execute is called with Koin strategy") {
            Then("it should merge repository module with useKoin=true") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val diRepo = mockk<DiModuleRepository>()
                val step = UpdateRepositoryDiStep(modulePkgRepo, diRepo)

                every { modulePkgRepo.findModulePackage(diDir) } returns "com.example.di"
                every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.data"
                every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.domain"

                coEvery {
                    diRepo.mergeRepositoryModule(any(), any(), any(), any(), any(), any())
                } returns MergeOutcome(diDir.resolve("RepositoryModule.kt"), "Updated")

                step.execute(params(DiStrategy.Koin(useAnnotations = false))).shouldBeInstanceOf<StepResult.Success>()

                coVerify(exactly = 1) {
                    diRepo.mergeRepositoryModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        className = "MyRepository",
                        domainFqn = "com.example.domain.repository",
                        dataFqn = "com.example.data.repository",
                        useKoin = true,
                    )
                }
            }
        }

        When("di package has .repository or .usecase suffix") {
            Then("it should strip the suffix") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val diRepo = mockk<DiModuleRepository>()
                val step = UpdateRepositoryDiStep(modulePkgRepo, diRepo)

                every { modulePkgRepo.findModulePackage(diDir) } returns "com.example.di.repository"
                every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.data"
                every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.domain"

                coEvery {
                    diRepo.mergeRepositoryModule(any(), any(), any(), any(), any(), any())
                } returns MergeOutcome(diDir.resolve("RepositoryModule.kt"), "Updated")

                step.execute(params(DiStrategy.Hilt)).shouldBeInstanceOf<StepResult.Success>()

                coVerify(exactly = 1) {
                    diRepo.mergeRepositoryModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        className = "MyRepository",
                        domainFqn = "com.example.domain.repository",
                        dataFqn = "com.example.data.repository",
                        useKoin = false,
                    )
                }
            }
        }

        When("modulePkgRepo throws") {
            Then("it returns Failure") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val diRepo = mockk<DiModuleRepository>()
                val step = UpdateRepositoryDiStep(modulePkgRepo, diRepo)

                every { modulePkgRepo.findModulePackage(any()) } throws IllegalStateException("boom")

                step.execute(params(DiStrategy.Hilt)).shouldBeInstanceOf<StepResult.Failure>()
            }
        }
    }
})
