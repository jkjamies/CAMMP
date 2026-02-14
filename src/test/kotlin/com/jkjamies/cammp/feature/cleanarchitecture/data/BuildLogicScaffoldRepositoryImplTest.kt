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

import com.jkjamies.cammp.feature.cleanarchitecture.data.factory.BuildLogicSpecFactoryImpl
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.domain.model.DatasourceStrategy
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.AliasesRepositoryWritingFake
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.ConventionPluginRepositoryWritingFake
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path

/**
 * Tests for [BuildLogicScaffoldRepositoryImpl].
 */
class BuildLogicScaffoldRepositoryImplTest : BehaviorSpec({

    fun params(base: Path) = CleanArchitectureParams(
        projectBasePath = base,
        root = "feature",
        feature = "profile",
        orgCenter = "com.example",
        includePresentation = true,
        includeDiModule = true,
        datasourceStrategy = DatasourceStrategy.RemoteAndLocal,
        diStrategy = DiStrategy.Hilt,
    )

    Given("BuildLogicScaffoldRepositoryImpl") {
        val fs = FileSystemRepositoryImpl()
        val templateRepo = TemplateRepositoryImpl()
        val buildLogicSpecFactory = BuildLogicSpecFactoryImpl()

        When("build-logic directory does not exist") {
            Then("it should scaffold baseline structure and report changed=true") {
                withTempDir("cammp_buildlogic") { tmp: Path ->
                    val repo = BuildLogicScaffoldRepositoryImpl(
                        fs = fs,
                        templateRepo = templateRepo,
                        aliasesRepo = AliasesRepositoryWritingFake(fs),
                        conventionPluginRepo = ConventionPluginRepositoryWritingFake(fs),
                        buildLogicSpecFactory = buildLogicSpecFactory,
                    )

                    val changed = repo.ensureBuildLogic(
                        params = params(tmp),
                        enabledModules = listOf("domain", "data", "di", "presentation", "remoteDataSource", "localDataSource"),
                        diMode = DiMode.HILT,
                    )

                    changed shouldBe true

                    // Baseline files
                    fs.exists(tmp.resolve("build-logic/settings.gradle.kts")) shouldBe true
                    fs.exists(tmp.resolve("build-logic/build.gradle.kts")) shouldBe true

                    // Helpers + core
                    fs.exists(tmp.resolve("build-logic/src/main/kotlin/com/example/convention/helpers/AndroidLibraryDefaults.kt")) shouldBe true
                    fs.exists(tmp.resolve("build-logic/src/main/kotlin/com/example/convention/helpers/StandardTestDependencies.kt")) shouldBe true
                    fs.exists(tmp.resolve("build-logic/src/main/kotlin/com/example/convention/helpers/TestOptions.kt")) shouldBe true
                    fs.exists(tmp.resolve("build-logic/src/main/kotlin/com/example/convention/core/Dependencies.kt")) shouldBe true

                    // We don't assert on Aliases.kt / plugin outputs here because those are delegated to other repos.
                }
            }
        }
    }
})
