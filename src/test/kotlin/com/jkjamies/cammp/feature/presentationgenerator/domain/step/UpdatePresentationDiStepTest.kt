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
import com.jkjamies.cammp.feature.presentationgenerator.testutil.PresentationDiModuleRepositoryFake
import com.jkjamies.cammp.feature.presentationgenerator.testutil.TestFiles
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.io.path.createDirectories

/**
 * Tests for [UpdatePresentationDiStep].
 */
class UpdatePresentationDiStepTest : BehaviorSpec({

    fun modulePkg(pkg: String? = "com.example.presentation") = ModulePackageRepositoryFake(pkg)

    fun params(
        moduleDir: java.nio.file.Path,
        screenName: String = "Home",
        diStrategy: DiStrategy,
        selectedUseCases: List<String> = emptyList(),
    ) = PresentationParams(
        moduleDir = moduleDir,
        screenName = screenName,
        patternStrategy = PresentationPatternStrategy.MVVM,
        diStrategy = diStrategy,
        selectedUseCases = selectedUseCases,
    )

    fun step(pkg: String?, diRepo: PresentationDiModuleRepositoryFake) =
        UpdatePresentationDiStep(modulePkg(pkg), diRepo)

    Given("UpdatePresentationDiStep") {

        When("di strategy is not Koin") {
            Then("it should skip") {
                TestFiles.withTempDir("pg_step_di") { moduleDir ->
                    val diRepo = PresentationDiModuleRepositoryFake()
                    val step = step("com.example.presentation", diRepo)

                    val result = step.execute(
                        params(
                            moduleDir = moduleDir,
                            diStrategy = DiStrategy.Hilt,
                        )
                    )

                    result.shouldBeInstanceOf<StepResult.Success>()
                    result.message shouldBe null
                    diRepo.calls.size shouldBe 0
                }
            }
        }

        When("di strategy is Koin with annotations") {
            Then("it should skip") {
                TestFiles.withTempDir("pg_step_di") { moduleDir ->
                    val diRepo = PresentationDiModuleRepositoryFake()
                    val step = step("com.example.presentation", diRepo)

                    val result = step.execute(
                        params(
                            moduleDir = moduleDir,
                            diStrategy = DiStrategy.Koin(useAnnotations = true),
                        )
                    )

                    result.shouldBeInstanceOf<StepResult.Success>()
                    result.message shouldBe null
                    diRepo.calls.size shouldBe 0
                }
            }
        }

        When("di module does not exist") {
            Then("it should skip") {
                TestFiles.withTempDir("pg_step_di") { moduleDir ->
                    val diRepo = PresentationDiModuleRepositoryFake()
                    val step = step("com.example.presentation", diRepo)

                    val result = step.execute(
                        params(
                            moduleDir = moduleDir,
                            diStrategy = DiStrategy.Koin(useAnnotations = false),
                        )
                    )

                    result.shouldBeInstanceOf<StepResult.Success>()
                    result.message shouldBe null
                    diRepo.calls.size shouldBe 0
                }
            }
        }

        When("koin without annotations and di module exists") {
            Then("it should merge view model module") {
                TestFiles.withTempDir("pg_step_di") { root ->
                    // step expects moduleDir.parent/di to exist
                    val moduleDir = root.resolve("presentation").also { it.createDirectories() }
                    val diDir = root.resolve("di").also { it.createDirectories() }

                    val diRepo = PresentationDiModuleRepositoryFake(status = "updated")
                    val step = step("com.example.account.profile.presentation", diRepo)

                    val result = step.execute(
                        params(
                            moduleDir = moduleDir,
                            diStrategy = DiStrategy.Koin(useAnnotations = false),
                            selectedUseCases = listOf("b", "a", "a"),
                        )
                    )

                    result.shouldBeInstanceOf<StepResult.Success>()
                    diRepo.calls.size shouldBe 1

                    val call = diRepo.calls.single()
                    call.diDir shouldBe diDir
                    call.diPackage shouldBe "com.example.account.profile.di"
                    call.viewModelSimpleName shouldBe "HomeViewModel"
                    call.viewModelFqn shouldBe "com.example.account.profile.presentation.home.HomeViewModel"
                    call.dependencyCount shouldBe 2
                }
            }
        }

        When("module package is missing and di dir exists") {
            Then("it should still merge using default package") {
                TestFiles.withTempDir("pg_step_di") { root ->
                    val moduleDir = root.resolve("presentation").also { it.createDirectories() }
                    val diDir = root.resolve("di").also { it.createDirectories() }

                    val diRepo = PresentationDiModuleRepositoryFake(status = "created")
                    val step = step(null, diRepo)

                    val result = step.execute(
                        params(
                            moduleDir = moduleDir,
                            diStrategy = DiStrategy.Koin(useAnnotations = false),
                        )
                    )

                    result.shouldBeInstanceOf<StepResult.Success>()
                    diRepo.calls.size shouldBe 1

                    val call = diRepo.calls.single()
                    call.diDir shouldBe diDir
                    call.diPackage shouldBe "com.example.di"
                }
            }
        }
    }
})
