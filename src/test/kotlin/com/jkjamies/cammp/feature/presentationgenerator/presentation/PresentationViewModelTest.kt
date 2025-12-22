package com.jkjamies.cammp.feature.presentationgenerator.presentation

import app.cash.turbine.test
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.usecase.PresentationGenerator
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import java.nio.file.Paths

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
class PresentationViewModelTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)
    
    val generator = mockk<PresentationGenerator>()
    val viewModel = PresentationViewModel(
        generator = generator,
        scope = testScope
    )

    afterSpec {
        clearAllMocks()
    }

    Given("a presentation view model") {

        When("a directory is set") {
            val intent = PresentationIntent.SetDirectory("/test/dir")
            viewModel.handleIntent(intent)

            Then("the state should be updated") {
                viewModel.state.test {
                    awaitItem().directory shouldBe "/test/dir"
                }
            }
        }

        When("a screen name is set") {
            val intent = PresentationIntent.SetScreenName("MyScreen")
            viewModel.handleIntent(intent)

            Then("the state should be updated") {
                viewModel.state.test {
                    awaitItem().screenName shouldBe "MyScreen"
                }
            }
        }

        When("a use case is toggled") {
            val fqn = "com.example.UseCase"
            viewModel.handleIntent(PresentationIntent.ToggleUseCaseSelection(fqn, true))

            Then("the use case should be added to selected") {
                viewModel.state.test {
                    awaitItem().selectedUseCases shouldContain fqn
                }
            }

            viewModel.handleIntent(PresentationIntent.ToggleUseCaseSelection(fqn, false))
            Then("the use case should be removed from selected") {
                viewModel.state.test {
                    awaitItem().selectedUseCases.contains(fqn) shouldBe false
                }
            }
        }

        When("generating with a blank directory") {
            viewModel.handleIntent(PresentationIntent.Generate)

            Then("an error message should be set") {
                viewModel.state.test {
                    awaitItem().errorMessage shouldBe "Directory is required"
                }
            }
        }

        When("generating with a blank screen name") {
            viewModel.handleIntent(PresentationIntent.SetDirectory("/test/dir"))
            viewModel.handleIntent(PresentationIntent.Generate)

            Then("an error message should be set") {
                viewModel.state.test {
                    awaitItem().errorMessage shouldBe "Screen name is required"
                }
            }
        }

        When("generation is successful") {
            val params = PresentationParams(
                moduleDir = Paths.get("/test/dir"),
                screenName = "MyScreen",
                patternMVI = true,
                patternMVVM = false,
                diHilt = true,
                diKoin = false,
                diKoinAnnotations = false,
                includeNavigation = false,
                useFlowStateHolder = false,
                useScreenStateHolder = false,
                selectedUseCases = emptyList()
            )
            val presResult = PresentationResult(
                created = listOf("file1.kt"),
                skipped = listOf("file2.kt"),
                message = "Success"
            )
            coEvery { generator(params) } returns Result.success(presResult)

            viewModel.handleIntent(PresentationIntent.SetDirectory("/test/dir"))
            viewModel.handleIntent(PresentationIntent.SetScreenName("MyScreen"))
            viewModel.handleIntent(PresentationIntent.SetPatternMVI(true))
            viewModel.handleIntent(PresentationIntent.SetDiHilt(true))
            viewModel.handleIntent(PresentationIntent.Generate)

            Then("the state should reflect the successful result") {
                viewModel.state.test {
                    // First item is the current state (isGenerating = true)
                    val loadingState = awaitItem()
                    loadingState.isGenerating shouldBe true

                    // Advance past the generation coroutine
                    testScope.advanceUntilIdle()

                    // Next item is the success state
                    val finalState = awaitItem()
                    finalState.isGenerating shouldBe false
                    finalState.lastMessage shouldBe "Success"
                    finalState.lastCreated shouldBe listOf("file1.kt")
                    finalState.lastSkipped shouldBe listOf("file2.kt")
                }
            }
        }

        When("generation fails") {
            val params = PresentationParams(
                moduleDir = Paths.get("/test/dir"),
                screenName = "MyScreen",
                patternMVI = true,
                patternMVVM = false,
                diHilt = true,
                diKoin = false,
                diKoinAnnotations = false,
                includeNavigation = false,
                useFlowStateHolder = false,
                useScreenStateHolder = false,
                selectedUseCases = emptyList()
            )
            coEvery { generator(params) } returns Result.failure(Exception("Generation failed"))

            viewModel.handleIntent(PresentationIntent.SetDirectory("/test/dir"))
            viewModel.handleIntent(PresentationIntent.SetScreenName("MyScreen"))
            viewModel.handleIntent(PresentationIntent.SetPatternMVI(true))
            viewModel.handleIntent(PresentationIntent.SetDiHilt(true))
            viewModel.handleIntent(PresentationIntent.Generate)

            Then("the state should reflect the failure") {
                viewModel.state.test {
                    // First item is the current state (isGenerating = true)
                    val loadingState = awaitItem()
                    loadingState.isGenerating shouldBe true

                    // Advance past the generation coroutine
                    testScope.advanceUntilIdle()

                    // Next item is the failure state
                    val finalState = awaitItem()
                    finalState.isGenerating shouldBe false
                    finalState.errorMessage shouldBe "Generation failed"
                }
            }
        }
    }
})
