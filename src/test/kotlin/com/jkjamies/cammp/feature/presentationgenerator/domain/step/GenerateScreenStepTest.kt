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

package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.domain.step.StepResult

import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.testutil.ModulePackageRepositoryFake
import com.jkjamies.cammp.feature.presentationgenerator.testutil.FlowStateHolderRepositoryFake
import com.jkjamies.cammp.feature.presentationgenerator.testutil.IntentRepositoryFake
import com.jkjamies.cammp.feature.presentationgenerator.testutil.NavigationRepositoryFake
import com.jkjamies.cammp.feature.presentationgenerator.testutil.ScreenRepositoryFake
import com.jkjamies.cammp.feature.presentationgenerator.testutil.ScreenStateHolderRepositoryFake
import com.jkjamies.cammp.feature.presentationgenerator.testutil.UiStateRepositoryFake
import com.jkjamies.cammp.feature.presentationgenerator.testutil.ViewModelRepositoryFake
import com.jkjamies.cammp.feature.presentationgenerator.testutil.TestFiles
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for [GenerateScreenStep].
 */
class GenerateScreenStepTest : BehaviorSpec({

    // Shared builders (fresh instances each call -> safe for future concurrency)
    fun modulePkg(pkg: String? = "com.example.presentation") = ModulePackageRepositoryFake(pkg)

    fun params(
        moduleDir: java.nio.file.Path,
        screenName: String = "Home",
        pattern: PresentationPatternStrategy = PresentationPatternStrategy.MVVM,
        di: DiStrategy = DiStrategy.Hilt,
        includeNavigation: Boolean = false,
        useFlowStateHolder: Boolean = false,
        useScreenStateHolder: Boolean = false,
    ) = PresentationParams(
        moduleDir = moduleDir,
        screenName = screenName,
        patternStrategy = pattern,
        diStrategy = di,
        includeNavigation = includeNavigation,
        useFlowStateHolder = useFlowStateHolder,
        useScreenStateHolder = useScreenStateHolder,
    )

    Given("presentation generation steps") {

        When("GenerateScreenStep executes") {
            Then("it should create src/main/kotlin, infer package, and call repository") {
                TestFiles.withTempDir("pg_step_screen") { moduleDir ->
                    val repo = ScreenRepositoryFake()
                    val step = GenerateScreenStep(modulePkg("com.example.presentation"), repo)

                    val result = step.execute(
                        params(
                            moduleDir = moduleDir,
                            screenName = "Home Screen",
                        )
                    )
                    result.shouldBeInstanceOf<StepResult.Success>()

                    repo.calls.size shouldBe 1
                    val call = repo.calls.single()
                    call.packageName shouldBe "com.example.presentation.homeScreen"
                    call.targetDir.endsWith("src/main/kotlin/com/example/presentation/homeScreen") shouldBe true
                }
            }
        }

        When("GenerateUiStateStep executes") {
            Then("it should call repository") {
                TestFiles.withTempDir("pg_step_uistate") { moduleDir ->
                    val repo = UiStateRepositoryFake()
                    val step = GenerateUiStateStep(modulePkg("com.example.presentation"), repo)

                    step.execute(params(moduleDir = moduleDir)).shouldBeInstanceOf<StepResult.Success>()
                    repo.calls.single().packageName shouldBe "com.example.presentation.home"
                }
            }
        }

        When("GenerateViewModelStep executes") {
            Then("it should call repository") {
                TestFiles.withTempDir("pg_step_vm") { moduleDir ->
                    val repo = ViewModelRepositoryFake()
                    val step = GenerateViewModelStep(modulePkg("com.example.presentation"), repo)

                    step.execute(params(moduleDir = moduleDir)).shouldBeInstanceOf<StepResult.Success>()
                    repo.calls.single().packageName shouldBe "com.example.presentation.home"
                }
            }
        }

        When("GenerateIntentStep used with MVVM") {
            Then("it should skip") {
                TestFiles.withTempDir("pg_step_intent") { moduleDir ->
                    val repo = IntentRepositoryFake()
                    val step = GenerateIntentStep(modulePkg("com.example.presentation"), repo)

                    val result = step.execute(params(moduleDir = moduleDir, pattern = PresentationPatternStrategy.MVVM))
                    result.shouldBeInstanceOf<StepResult.Success>()
                    repo.calls.size shouldBe 0
                }
            }
        }

        When("GenerateIntentStep used with MVI") {
            Then("it should generate") {
                TestFiles.withTempDir("pg_step_intent") { moduleDir ->
                    val repo = IntentRepositoryFake()
                    val step = GenerateIntentStep(modulePkg("com.example.presentation"), repo)

                    step.execute(params(moduleDir = moduleDir, pattern = PresentationPatternStrategy.MVI))
                        .shouldBeInstanceOf<StepResult.Success>()
                    repo.calls.size shouldBe 1
                }
            }
        }

        When("GenerateScreenStateHolderStep disabled") {
            Then("it should skip") {
                TestFiles.withTempDir("pg_step_ssholder") { moduleDir ->
                    val repo = ScreenStateHolderRepositoryFake()
                    val step = GenerateScreenStateHolderStep(modulePkg("com.example.presentation"), repo)

                    step.execute(params(moduleDir = moduleDir, useScreenStateHolder = false))
                        .shouldBeInstanceOf<StepResult.Success>()
                    repo.calls.size shouldBe 0
                }
            }
        }

        When("GenerateScreenStateHolderStep enabled") {
            Then("it should generate") {
                TestFiles.withTempDir("pg_step_ssholder") { moduleDir ->
                    val repo = ScreenStateHolderRepositoryFake()
                    val step = GenerateScreenStateHolderStep(modulePkg("com.example.presentation"), repo)

                    step.execute(params(moduleDir = moduleDir, useScreenStateHolder = true))
                        .shouldBeInstanceOf<StepResult.Success>()
                    repo.calls.size shouldBe 1
                }
            }
        }

        When("GenerateFlowStateHolderStep disabled") {
            Then("it should skip") {
                TestFiles.withTempDir("pg_step_flow") { moduleDir ->
                    val repo = FlowStateHolderRepositoryFake()
                    val step = GenerateFlowStateHolderStep(modulePkg("com.example.presentation"), repo)

                    step.execute(params(moduleDir = moduleDir, useFlowStateHolder = false))
                        .shouldBeInstanceOf<StepResult.Success>()
                    repo.calls.size shouldBe 0
                }
            }
        }

        When("GenerateFlowStateHolderStep enabled") {
            Then("it should generate a module-level flow state holder") {
                TestFiles.withTempDir("pg_step_flow") { moduleDir ->
                    val repo = FlowStateHolderRepositoryFake()
                    val step = GenerateFlowStateHolderStep(modulePkg("com.example.account.profile.presentation"), repo)

                    step.execute(params(moduleDir = moduleDir, useFlowStateHolder = true))
                        .shouldBeInstanceOf<StepResult.Success>()
                    repo.calls.single().flowName shouldBe "ProfileFlowStateHolder"
                }
            }
        }

        When("GenerateNavigationHostStep disabled") {
            Then("it should skip") {
                TestFiles.withTempDir("pg_step_nav") { moduleDir ->
                    val repo = NavigationRepositoryFake()
                    val step = GenerateNavigationHostStep(modulePkg("com.example.presentation"), repo)

                    step.execute(params(moduleDir = moduleDir, includeNavigation = false))
                        .shouldBeInstanceOf<StepResult.Success>()
                    repo.hostCalls.size shouldBe 0
                }
            }
        }

        When("GenerateNavigationHostStep enabled") {
            Then("it should create navigation host") {
                TestFiles.withTempDir("pg_step_nav") { moduleDir ->
                    val repo = NavigationRepositoryFake()
                    val step = GenerateNavigationHostStep(modulePkg("com.example.account.profile.presentation"), repo)

                    step.execute(params(moduleDir = moduleDir, includeNavigation = true))
                        .shouldBeInstanceOf<StepResult.Success>()
                    repo.hostCalls.single().packageName shouldBe "com.example.account.profile.presentation.navigation"
                    repo.hostCalls.single().navHostName shouldBe "ProfileNavigationHost"
                }
            }
        }

        When("GenerateNavigationDestinationStep disabled") {
            Then("it should skip") {
                TestFiles.withTempDir("pg_step_navdest") { moduleDir ->
                    val repo = NavigationRepositoryFake()
                    val step = GenerateNavigationDestinationStep(modulePkg("com.example.presentation"), repo)

                    step.execute(params(moduleDir = moduleDir, includeNavigation = false))
                        .shouldBeInstanceOf<StepResult.Success>()
                    repo.destinationCalls.size shouldBe 0
                }
            }
        }

        When("GenerateNavigationDestinationStep enabled") {
            Then("it should create destination under navigation/destinations") {
                TestFiles.withTempDir("pg_step_navdest") { moduleDir ->
                    val repo = NavigationRepositoryFake()
                    val step = GenerateNavigationDestinationStep(modulePkg("com.example.presentation"), repo)

                    step.execute(
                        params(
                            moduleDir = moduleDir,
                            screenName = "Home Screen",
                            includeNavigation = true,
                        )
                    ).shouldBeInstanceOf<StepResult.Success>()
                    repo.destinationCalls.single().screenFolder shouldBe "homeScreen"
                }
            }
        }
    }
})
