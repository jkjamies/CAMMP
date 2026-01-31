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

package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.GradleSettingsDataSourceFake
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

class GradleSettingsScaffoldRepositoryImplTest : BehaviorSpec({

    fun params(base: java.nio.file.Path) = CleanArchitectureParams(
        projectBasePath = base,
        root = "feature",
        feature = "my-feature",
        orgCenter = "com.example",
        includePresentation = true,
        includeDiModule = true,
        datasourceStrategy = DatasourceStrategy.None,
        diStrategy = DiStrategy.Hilt,
    )

    Given("GradleSettingsScaffoldRepositoryImpl") {
        When("no underlying changes are needed") {
            Then("it should return false") {
                val tmp = Files.createTempDirectory("cammp_settings")
                val dataSource = GradleSettingsDataSourceFake(
                    includesChanged = false,
                    includeBuildChanged = false,
                    aliasesChanged = false,
                    appDepChanged = false,
                )
                val repo = GradleSettingsScaffoldRepositoryImpl(dataSource)

                val changed = repo.ensureSettings(
                    params = params(tmp),
                    enabledModules = listOf("domain", "data"),
                    diMode = DiMode.HILT,
                )

                changed shouldBe false
                dataSource.ensureIncludesCalls.size shouldBe 1
                dataSource.ensureIncludeBuildCalls.size shouldBe 1
                dataSource.ensureVersionCatalogPluginAliasesCalls.size shouldBe 1
                dataSource.ensureAppDependencyCalls.size shouldBe 1

                // best-effort cleanup
                tmp.toFile().deleteRecursively()
            }
        }

        When("any underlying settings update occurs") {
            Then("it should return true") {
                val tmp = Files.createTempDirectory("cammp_settings2")
                val dataSource = GradleSettingsDataSourceFake(
                    includesChanged = false,
                    includeBuildChanged = true,
                    aliasesChanged = false,
                    appDepChanged = false,
                )
                val repo = GradleSettingsScaffoldRepositoryImpl(dataSource)

                val changed = repo.ensureSettings(
                    params = params(tmp),
                    enabledModules = listOf("domain"),
                    diMode = DiMode.KOIN,
                )

                changed shouldBe true

                // best-effort cleanup
                tmp.toFile().deleteRecursively()
            }
        }
    }
})
