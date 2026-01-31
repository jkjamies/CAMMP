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

package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.data.factory.FlowStateHolderSpecFactoryImpl
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.IntentSpecFactoryImpl
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.NavigationSpecFactoryImpl
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.ScreenSpecFactoryImpl
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.ScreenStateHolderSpecFactoryImpl
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.UiStateSpecFactoryImpl
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.ViewModelSpecFactoryImpl
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.testutil.TestFiles
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.io.path.createDirectories
import kotlin.io.path.readText

/**
 * Tests for [ScreenRepositoryImpl].
 */
class ScreenRepositoryImplTest : BehaviorSpec({

    Given("repository implementations") {

        When("ScreenRepositoryImpl writes a file") {
            Then("it should create content and return CREATED") {
                TestFiles.withTempDir("pg_repo_screen") { dir ->
                    val repo = ScreenRepositoryImpl(ScreenSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVVM,
                        diStrategy = DiStrategy.Hilt,
                    )

                    val result = repo.generateScreen(outDir, "com.example", params)
                    result.status shouldBe GenerationStatus.CREATED

                    val text = result.path.readText()
                    text.contains("package com.example") shouldBe true
                    text.contains("fun Home") shouldBe true
                }
            }
        }

        When("ScreenRepositoryImpl target exists") {
            Then("it should return SKIPPED") {
                TestFiles.withTempDir("pg_repo_screen") { dir ->
                    val repo = ScreenRepositoryImpl(ScreenSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }
                    outDir.resolve("Home.kt").toFile().writeText("existing")

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVVM,
                        diStrategy = DiStrategy.Hilt,
                    )

                    val result = repo.generateScreen(outDir, "com.example", params)
                    result.status shouldBe GenerationStatus.SKIPPED
                }
            }
        }

        When("UiStateRepositoryImpl writes a file") {
            Then("it should create content and return CREATED") {
                TestFiles.withTempDir("pg_repo_uistate") { dir ->
                    val repo = UiStateRepositoryImpl(UiStateSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVVM,
                        diStrategy = DiStrategy.Hilt,
                    )

                    val result = repo.generateUiState(outDir, "com.example", params)
                    result.status shouldBe GenerationStatus.CREATED

                    val text = result.path.readText()
                    text.contains("data class HomeUiState") shouldBe true
                    text.contains("isLoading") shouldBe true
                }
            }
        }

        When("UiStateRepositoryImpl target exists") {
            Then("it should return SKIPPED") {
                TestFiles.withTempDir("pg_repo_uistate") { dir ->
                    val repo = UiStateRepositoryImpl(UiStateSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }
                    outDir.resolve("HomeUiState.kt").toFile().writeText("existing")

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVVM,
                        diStrategy = DiStrategy.Hilt,
                    )

                    repo.generateUiState(outDir, "com.example", params).status shouldBe GenerationStatus.SKIPPED
                }
            }
        }

        When("IntentRepositoryImpl writes a file") {
            Then("it should create content") {
                TestFiles.withTempDir("pg_repo_intent") { dir ->
                    val repo = IntentRepositoryImpl(IntentSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVI,
                        diStrategy = DiStrategy.Hilt,
                    )

                    val result = repo.generateIntent(outDir, "com.example", params)
                    result.status shouldBe GenerationStatus.CREATED
                    result.path.readText().contains("sealed interface HomeIntent") shouldBe true
                }
            }
        }

        When("IntentRepositoryImpl target exists") {
            Then("it should return SKIPPED") {
                TestFiles.withTempDir("pg_repo_intent") { dir ->
                    val repo = IntentRepositoryImpl(IntentSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }
                    outDir.resolve("HomeIntent.kt").toFile().writeText("existing")

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVI,
                        diStrategy = DiStrategy.Hilt,
                    )

                    repo.generateIntent(outDir, "com.example", params).status shouldBe GenerationStatus.SKIPPED
                }
            }
        }

        When("ViewModelRepositoryImpl writes a file") {
            Then("it should create content") {
                TestFiles.withTempDir("pg_repo_vm") { dir ->
                    val repo = ViewModelRepositoryImpl(ViewModelSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVI,
                        diStrategy = DiStrategy.Hilt,
                        selectedUseCases = listOf("com.example.domain.usecase.GetStuff")
                    )

                    val result = repo.generateViewModel(outDir, "com.example", params)
                    result.status shouldBe GenerationStatus.CREATED

                    val text = result.path.readText()
                    text.contains("class HomeViewModel") shouldBe true
                }
            }
        }

        When("ViewModelRepositoryImpl target exists") {
            Then("it should return SKIPPED") {
                TestFiles.withTempDir("pg_repo_vm") { dir ->
                    val repo = ViewModelRepositoryImpl(ViewModelSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }
                    outDir.resolve("HomeViewModel.kt").toFile().writeText("existing")

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVI,
                        diStrategy = DiStrategy.Hilt,
                        selectedUseCases = listOf("com.example.domain.usecase.GetStuff")
                    )

                    repo.generateViewModel(outDir, "com.example", params).status shouldBe GenerationStatus.SKIPPED
                }
            }
        }

        When("FlowStateHolderRepositoryImpl writes a file") {
            Then("it should create content") {
                TestFiles.withTempDir("pg_repo_flow") { dir ->
                    val repo = FlowStateHolderRepositoryImpl(FlowStateHolderSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVVM,
                        diStrategy = DiStrategy.Hilt,
                    )

                    val result = repo.generateFlowStateHolder(outDir, "com.example", "FeatureFlowStateHolder", params)
                    result.status shouldBe GenerationStatus.CREATED
                    result.path.readText().contains("class FeatureFlowStateHolder") shouldBe true
                }
            }
        }

        When("FlowStateHolderRepositoryImpl target exists") {
            Then("it should return SKIPPED") {
                TestFiles.withTempDir("pg_repo_flow") { dir ->
                    val repo = FlowStateHolderRepositoryImpl(FlowStateHolderSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }
                    outDir.resolve("FeatureFlowStateHolder.kt").toFile().writeText("existing")

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVVM,
                        diStrategy = DiStrategy.Hilt,
                    )

                    repo.generateFlowStateHolder(outDir, "com.example", "FeatureFlowStateHolder", params)
                        .status shouldBe GenerationStatus.SKIPPED
                }
            }
        }

        When("ScreenStateHolderRepositoryImpl writes a file") {
            Then("it should create content") {
                TestFiles.withTempDir("pg_repo_ssholder") { dir ->
                    val repo = ScreenStateHolderRepositoryImpl(ScreenStateHolderSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVVM,
                        diStrategy = DiStrategy.Hilt,
                    )

                    val result = repo.generateScreenStateHolder(outDir, "com.example", params)
                    result.status shouldBe GenerationStatus.CREATED
                    result.path.readText().contains("class HomeStateHolder") shouldBe true
                }
            }
        }

        When("ScreenStateHolderRepositoryImpl target exists") {
            Then("it should return SKIPPED") {
                TestFiles.withTempDir("pg_repo_ssholder") { dir ->
                    val repo = ScreenStateHolderRepositoryImpl(ScreenStateHolderSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }
                    outDir.resolve("HomeStateHolder.kt").toFile().writeText("existing")

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVVM,
                        diStrategy = DiStrategy.Hilt,
                    )

                    repo.generateScreenStateHolder(outDir, "com.example", params).status shouldBe GenerationStatus.SKIPPED
                }
            }
        }

        When("NavigationRepositoryImpl creates host + destination") {
            Then("it should write both files and include destination comments") {
                TestFiles.withTempDir("pg_repo_nav") { dir ->
                    val repo = NavigationRepositoryImpl(NavigationSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }

                    val host = repo.generateNavigationHost(outDir, "com.example.navigation", "ProfileNavigationHost")
                    host.status shouldBe GenerationStatus.CREATED
                    host.path.readText().contains("fun ProfileNavigationHost") shouldBe true

                    val dest = repo.generateDestination(
                        targetDir = outDir,
                        packageName = "com.example.presentation",
                        params = PresentationParams(
                            moduleDir = dir,
                            screenName = "Home",
                            patternStrategy = PresentationPatternStrategy.MVVM,
                            diStrategy = DiStrategy.Hilt,
                        ),
                        screenFolder = "home"
                    )
                    dest.status shouldBe GenerationStatus.CREATED
                    val destText = dest.path.readText()
                    destText.contains("internal object HomeDestination") shouldBe true
                    destText.contains("example if you need to pass parameters") shouldBe true
                }
            }
        }

        When("NavigationRepositoryImpl host target exists") {
            Then("it should return SKIPPED") {
                TestFiles.withTempDir("pg_repo_nav") { dir ->
                    val repo = NavigationRepositoryImpl(NavigationSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }
                    outDir.resolve("ProfileNavigationHost.kt").toFile().writeText("existing")

                    repo.generateNavigationHost(outDir, "com.example.navigation", "ProfileNavigationHost").status shouldBe
                        GenerationStatus.SKIPPED
                }
            }
        }

        When("NavigationRepositoryImpl destination target exists") {
            Then("it should return SKIPPED") {
                TestFiles.withTempDir("pg_repo_nav") { dir ->
                    val repo = NavigationRepositoryImpl(NavigationSpecFactoryImpl())
                    val outDir = dir.resolve("out").also { it.createDirectories() }
                    outDir.resolve("HomeDestination.kt").toFile().writeText("existing")

                    val params = PresentationParams(
                        moduleDir = dir,
                        screenName = "Home",
                        patternStrategy = PresentationPatternStrategy.MVVM,
                        diStrategy = DiStrategy.Hilt,
                    )

                    repo.generateDestination(
                        targetDir = outDir,
                        packageName = "com.example.presentation",
                        params = params,
                        screenFolder = "home"
                    ).status shouldBe GenerationStatus.SKIPPED
                }
            }
        }
    }
})
