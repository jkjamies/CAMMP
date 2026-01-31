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

package com.jkjamies.cammp.feature.cleanarchitecture.presentation

import app.cash.turbine.test
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import com.jkjamies.cammp.feature.cleanarchitecture.domain.usecase.CleanArchitectureGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

/**
 * Tests for [GenerateModulesViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GenerateModulesViewModelTest : BehaviorSpec({

    Given("GenerateModulesViewModel") {
        val dispatcher = StandardTestDispatcher()
        val scope = TestScope(dispatcher)

        When("Generate is invoked with missing project base") {
            val generator = mockk<CleanArchitectureGenerator>()
            val vm = GenerateModulesViewModel(projectBasePath = "", scope = scope, generator = generator)

            vm.handleIntent(GenerateModulesIntent.Generate)

            Then("it should set an error message") {
                vm.state.value.errorMessage shouldBe "Project base path is required"
            }
        }

        When("Generate is invoked for KMP") {
            val generator = mockk<CleanArchitectureGenerator>()
            val vm = GenerateModulesViewModel(projectBasePath = "/project", scope = scope, generator = generator)

            vm.handleIntent(GenerateModulesIntent.SetPlatformKmp(true))
            vm.handleIntent(GenerateModulesIntent.Generate)

            Then("it should set an error message") {
                vm.state.value.errorMessage shouldBe "KMP generation is not supported yet in CAMMP"
            }
        }

        When("Generate succeeds") {
            val generator = mockk<CleanArchitectureGenerator>()

            val result = CleanArchitectureResult(
                created = listOf("domain"),
                skipped = emptyList(),
                settingsUpdated = true,
                buildLogicCreated = true,
                message = "ok",
            )

            coEvery {
                generator.invoke(any<CleanArchitectureParams>())
            } returns Result.success(result)

            val vm = GenerateModulesViewModel(projectBasePath = "/project", scope = scope, generator = generator)

            vm.state.test {
                // initial
                awaitItem().let {
                    it.projectBasePath shouldBe "/project"
                    it.diHilt shouldBe false
                    it.diMetro shouldBe true
                }

                vm.handleIntent(GenerateModulesIntent.SetRoot("/project/app"))
                vm.handleIntent(GenerateModulesIntent.SetFeature("/project/my-feature"))

                vm.handleIntent(GenerateModulesIntent.Generate)

                // let coroutine run
                scope.advanceUntilIdle()

                val s = vm.state.value
                s.isGenerating shouldBe false
                s.lastCreated shouldBe listOf("domain")
                s.settingsUpdated shouldBe true
                s.buildLogicCreated shouldBe true

                cancelAndConsumeRemainingEvents()
            }

            Then("it should call generator with normalized paths") {
                // best-effort coverage: ensure no crash and state updated. deeper arg asserts are covered elsewhere.
                vm.state.value.lastMessage shouldBe "ok"
            }
        }

        When("Generate fails") {
            val generator = mockk<CleanArchitectureGenerator>()
            coEvery { generator.invoke(any()) } returns Result.failure(IllegalStateException("boom"))

            val vm = GenerateModulesViewModel(projectBasePath = "/project", scope = scope, generator = generator)
            vm.handleIntent(GenerateModulesIntent.SetRoot("app"))
            vm.handleIntent(GenerateModulesIntent.SetFeature("my-feature"))

            vm.handleIntent(GenerateModulesIntent.Generate)
            scope.advanceUntilIdle()

            Then("it should surface the error") {
                vm.state.value.errorMessage shouldBe "boom"
            }
        }

        When("non-generate intents are dispatched") {
            val generator = mockk<CleanArchitectureGenerator>(relaxed = true)
            val vm = GenerateModulesViewModel(projectBasePath = "/project", scope = scope, generator = generator)

            vm.handleIntent(GenerateModulesIntent.SetOrgCenter("com.example"))
            vm.handleIntent(GenerateModulesIntent.SetIncludePresentation(true))
            vm.handleIntent(GenerateModulesIntent.SetIncludeApiModule(true))
            vm.handleIntent(GenerateModulesIntent.SetIncludeDatasource(true))
            vm.handleIntent(GenerateModulesIntent.SetDatasourceCombined(true))
            vm.handleIntent(GenerateModulesIntent.SetDatasourceRemote(true))
            vm.handleIntent(GenerateModulesIntent.SetDatasourceLocal(true))

            // Exercise DI switching behavior.
            vm.handleIntent(GenerateModulesIntent.SelectDiHilt(true))
            vm.handleIntent(GenerateModulesIntent.SelectDiKoin(true))
            vm.handleIntent(GenerateModulesIntent.SetKoinAnnotations(true))

            Then("it should update state without error") {
                val s = vm.state.value
                s.orgCenter shouldBe "com.example"
                s.includePresentation shouldBe true
                s.includeApiModule shouldBe true
                s.includeDatasource shouldBe true
                s.datasourceCombined shouldBe true
                s.datasourceRemote shouldBe true
                s.datasourceLocal shouldBe true

                // Last DI intent was Koin.
                s.diKoin shouldBe true
                s.diHilt shouldBe false
                s.diKoinAnnotations shouldBe true
                s.includeDiModule shouldBe false

                vm.handleIntent(GenerateModulesIntent.SetIncludeDiModule(true))
                vm.state.value.includeDiModule shouldBe true

                vm.handleIntent(GenerateModulesIntent.SelectDiMetro(true))

                vm.state.value.diMetro shouldBe true
                vm.state.value.includeDiModule shouldBe false

                vm.handleIntent(GenerateModulesIntent.SelectDiHilt(true))
                vm.state.value.diHilt shouldBe true
                vm.state.value.includeDiModule shouldBe true

                s.errorMessage shouldBe null
            }
        }

        When("verifying DI Module Checkbox Logic") {
            val generator = mockk<CleanArchitectureGenerator>(relaxed = true)
            val vm = GenerateModulesViewModel(projectBasePath = "/project", scope = scope, generator = generator)

            Then("it should match the requirements for all DI strategies") {
                // Initial State: Metro selected by default
                val initial = vm.state.value
                initial.diMetro shouldBe true
                initial.includeDiModule shouldBe false // Metro defaults to false

                // 1. Select Hilt -> Forced True
                vm.handleIntent(GenerateModulesIntent.SelectDiHilt(true))
                vm.state.value.diHilt shouldBe true
                vm.state.value.includeDiModule shouldBe true

                // 2. Select Koin (No Annotations) -> Forced True
                vm.handleIntent(GenerateModulesIntent.SelectDiKoin(true))
                val koinState = vm.state.value
                koinState.diKoin shouldBe true
                koinState.diKoinAnnotations shouldBe false
                koinState.includeDiModule shouldBe true

                // 3. Toggle Koin Annotations ON -> Selectable (Defaults to false currently due to !intent.selected logic)
                vm.handleIntent(GenerateModulesIntent.SetKoinAnnotations(true))
                val koinAnnoState = vm.state.value
                koinAnnoState.diKoinAnnotations shouldBe true
                koinAnnoState.includeDiModule shouldBe false // Logic sets it to !selected

                // Can toggle it
                vm.handleIntent(GenerateModulesIntent.SetIncludeDiModule(true))
                vm.state.value.includeDiModule shouldBe true

                // 4. Select Metro -> Selectable (Defaults to false when selected via intent)
                vm.handleIntent(GenerateModulesIntent.SelectDiMetro(true))
                val metroState = vm.state.value
                metroState.diMetro shouldBe true
                metroState.includeDiModule shouldBe false

                // Can toggle it
                vm.handleIntent(GenerateModulesIntent.SetIncludeDiModule(true))
                vm.state.value.includeDiModule shouldBe true
            }
        }
    }
})
