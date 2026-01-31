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

package com.jkjamies.cammp.feature.presentationgenerator.presentation

import app.cash.turbine.test
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.usecase.PresentationGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import java.nio.file.Paths

/**
 * Tests for [PresentationViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
class PresentationViewModelTest : BehaviorSpec({

    Given("PresentationViewModel") {

        When("setting simple fields") {
            Then("it should update state") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<PresentationGenerator>()
                val vm = PresentationViewModel(directory = "", scope = scope, generator = generator)

                vm.state.test {
                    awaitItem() // initial

                    vm.handleIntent(PresentationIntent.SetDirectory("/tmp"))
                    awaitItem().directory shouldBe "/tmp"

                    vm.handleIntent(PresentationIntent.SetScreenName("Home"))
                    awaitItem().screenName shouldBe "Home"

                    vm.handleIntent(PresentationIntent.SetPackage("com.example"))
                    awaitItem().pkg shouldBe "com.example"

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("toggling use case selection") {
            Then("it should add and remove fqns") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<PresentationGenerator>()
                val vm = PresentationViewModel(directory = "", scope = scope, generator = generator)

                val fqn = "com.example.UseCase"

                vm.state.test {
                    awaitItem()

                    vm.handleIntent(PresentationIntent.ToggleUseCaseSelection(fqn, true))
                    awaitItem().selectedUseCases shouldContain fqn

                    vm.handleIntent(PresentationIntent.ToggleUseCaseSelection(fqn, false))
                    awaitItem().selectedUseCases.contains(fqn) shouldBe false

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("pattern and DI selectors") {
            Then("they should be mutually exclusive") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<PresentationGenerator>()
                val vm = PresentationViewModel(directory = "", scope = scope, generator = generator)

                vm.state.test {
                    val initial = awaitItem()
                    initial.patternMVI shouldBe true
                    initial.patternMVVM shouldBe false
                    initial.patternCircuit shouldBe false
                    initial.diMetro shouldBe true
                    initial.diHilt shouldBe false
                    initial.diKoin shouldBe false

                    vm.handleIntent(PresentationIntent.SetPatternMVVM(true))
                    val mvvm = awaitItem()
                    mvvm.patternMVVM shouldBe true
                    mvvm.patternMVI shouldBe false
                    mvvm.patternCircuit shouldBe false

                    vm.handleIntent(PresentationIntent.SetPatternCircuit(true))
                    val circuit = awaitItem()
                    circuit.patternCircuit shouldBe true
                    circuit.patternMVI shouldBe false
                    circuit.patternMVVM shouldBe false

                    vm.handleIntent(PresentationIntent.SetPatternMVI(true))
                    val mvi = awaitItem()
                    mvi.patternMVI shouldBe true
                    mvi.patternMVVM shouldBe false
                    mvi.patternCircuit shouldBe false

                    vm.handleIntent(PresentationIntent.SetDiKoin(true))
                    val koin = awaitItem()
                    koin.diKoin shouldBe true
                    koin.diMetro shouldBe false
                    koin.diHilt shouldBe false

                    vm.handleIntent(PresentationIntent.ToggleKoinAnnotations(true))
                    awaitItem().diKoinAnnotations shouldBe true

                    vm.handleIntent(PresentationIntent.SetDiMetro(true))
                    val metro = awaitItem()
                    metro.diMetro shouldBe true
                    metro.diKoin shouldBe false
                    metro.diHilt shouldBe false
                    metro.diKoinAnnotations shouldBe false

                    vm.handleIntent(PresentationIntent.SetDiHilt(true))
                    val hilt = awaitItem()
                    hilt.diHilt shouldBe true
                    hilt.diMetro shouldBe false
                    hilt.diKoin shouldBe false
                    hilt.diKoinAnnotations shouldBe false

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("generating with invalid state") {
            Then("it should set error message and not call generator") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<PresentationGenerator>()
                val vm = PresentationViewModel(directory = "", scope = scope, generator = generator)

                vm.state.test {
                    awaitItem()
                    vm.handleIntent(PresentationIntent.Generate)
                    awaitItem().errorMessage shouldBe "Directory is required"
                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 0) { generator(any()) }
            }
        }

        When("generating succeeds") {
            Then("it should emit generating then success") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<PresentationGenerator>()
                val vm = PresentationViewModel(directory = "", scope = scope, generator = generator)

                val expectedParams = PresentationParams(
                    moduleDir = Paths.get("/tmp"),
                    screenName = "Home",
                    patternStrategy = PresentationPatternStrategy.MVI,
                    diStrategy = DiStrategy.Metro,
                    includeNavigation = true,
                    useFlowStateHolder = true,
                    useScreenStateHolder = true,
                    selectedUseCases = listOf("a", "b")
                )

                coEvery { generator(expectedParams) } returns Result.success("OK")

                vm.state.test {
                    awaitItem()

                    vm.handleIntent(PresentationIntent.SetDirectory("/tmp"))
                    awaitItem()
                    vm.handleIntent(PresentationIntent.SetScreenName("Home"))
                    awaitItem()

                    vm.handleIntent(PresentationIntent.ToggleIncludeNavigation(true))
                    awaitItem()
                    vm.handleIntent(PresentationIntent.ToggleFlowStateHolder(true))
                    awaitItem()
                    vm.handleIntent(PresentationIntent.ToggleScreenStateHolder(true))
                    awaitItem()

                    vm.handleIntent(PresentationIntent.SetUseCasesByModule(mapOf("m" to listOf("b", "a"))))
                    awaitItem()
                    vm.handleIntent(PresentationIntent.ToggleUseCaseSelection("b", true))
                    awaitItem()
                    vm.handleIntent(PresentationIntent.ToggleUseCaseSelection("a", true))
                    awaitItem()

                    vm.handleIntent(PresentationIntent.Generate)

                    awaitItem().isGenerating shouldBe true
                    scope.advanceUntilIdle()

                    val final = awaitItem()
                    final.isGenerating shouldBe false
                    final.lastMessage shouldBe "OK"
                    final.errorMessage shouldBe null

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { generator(expectedParams) }
            }
        }

        When("generating fails") {
            Then("it should surface the error") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<PresentationGenerator>()
                val vm = PresentationViewModel(directory = "", scope = scope, generator = generator)

                vm.handleIntent(PresentationIntent.SetDirectory("/tmp"))
                vm.handleIntent(PresentationIntent.SetScreenName("Home"))

                val expectedParams = PresentationParams(
                    moduleDir = Paths.get("/tmp"),
                    screenName = "Home",
                    patternStrategy = PresentationPatternStrategy.MVI,
                    diStrategy = DiStrategy.Metro,
                )

                coEvery { generator(expectedParams) } returns Result.failure(IllegalStateException("boom"))

                vm.state.test {
                    awaitItem()
                    // The two handleIntent calls above already updated state; we only care about generate emissions.
                    vm.handleIntent(PresentationIntent.Generate)
                    awaitItem().isGenerating shouldBe true
                    scope.advanceUntilIdle()
                    val final = awaitItem()
                    final.isGenerating shouldBe false
                    final.errorMessage shouldBe "boom"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
