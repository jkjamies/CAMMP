package com.jkjamies.cammp.feature.usecasegenerator.presentation

import app.cash.turbine.test
import com.jkjamies.cammp.feature.usecasegenerator.domain.usecase.LoadRepositories
import com.jkjamies.cammp.feature.usecasegenerator.domain.usecase.UseCaseGenerator
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import java.nio.file.Paths

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
class UseCaseViewModelTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf
    
    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)

    val mockGenerator = mockk<UseCaseGenerator>()
    val mockLoadRepositories = mockk<LoadRepositories>()
    
    val viewModel = UseCaseViewModel(
        generator = mockGenerator,
        loadRepositories = mockLoadRepositories,
        scope = testScope
    )

    afterSpec {
        clearAllMocks()
    }

    Given("a use case view model") {

        When("initialized") {
            Then("state should be empty") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.name shouldBe ""
                    state.domainPackage shouldBe ""
                }
            }
        }

        When("use case name is set") {
            viewModel.handleIntent(UseCaseIntent.SetName("GetItems"))
            Then("state should update use case name") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.name shouldBe "GetItems"
                }
            }
        }

        When("package name is set with valid domain module") {
            val path = "/path/to/domain"
            every { mockLoadRepositories(path) } returns listOf("Repo1", "Repo2")

            viewModel.handleIntent(UseCaseIntent.SetDomainPackage(path))

            Then("state should update package name and load repositories") {
                viewModel.state.test {
                    // Initial state
                    val loadingState = awaitItem()
                    loadingState.domainPackage shouldBe path

                    testScope.advanceUntilIdle()

                    // Final state
                    val finalState = awaitItem()
                    finalState.domainPackage shouldBe path
                    finalState.availableRepositories shouldBe listOf("Repo1", "Repo2")
                }
            }
        }

        When("package name is set with invalid module") {
            val path = "/path/to/invalid"
            viewModel.handleIntent(UseCaseIntent.SetDomainPackage(path))

            Then("state should update package name but clear repositories") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.domainPackage shouldBe path
                    state.availableRepositories shouldBe emptyList()
                }
            }
        }

        When("Async/Sync is toggled") {
            viewModel.handleIntent(UseCaseIntent.SetSync(true))
            Then("Async should be false and Sync true") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.async shouldBe false
                    state.sync shouldBe true
                }
            }

            viewModel.handleIntent(UseCaseIntent.SetAsync(true))
            Then("Async should be true and Sync false") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.async shouldBe true
                    state.sync shouldBe false
                }
            }
        }

        When("DI framework is toggled") {
            viewModel.handleIntent(UseCaseIntent.SetDiKoin(true))
            Then("Hilt should be disabled and Koin enabled") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.diHilt shouldBe false
                    state.diKoin shouldBe true
                }
            }
        }

        When("Repository selection is toggled") {
            viewModel.handleIntent(UseCaseIntent.ToggleRepository("UserRepo", true))
            Then("Selected repositories should contain UserRepo") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.selectedRepositories shouldBe setOf("UserRepo")
                }
            }

            viewModel.handleIntent(UseCaseIntent.ToggleRepository("UserRepo", false))
            Then("Selected repositories should be empty") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.selectedRepositories shouldBe emptySet()
                }
            }
        }

        When("Generate is called with invalid state") {
            viewModel.handleIntent(UseCaseIntent.SetName("")) // Invalid name
            viewModel.handleIntent(UseCaseIntent.Generate)
            Then("error message should be set") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.errorMessage shouldBe "Name is required"
                }
            }
        }

        When("Generate is called with valid state") {
            every { mockLoadRepositories(any()) } returns emptyList()
            viewModel.handleIntent(UseCaseIntent.SetName("ValidUseCase"))
            viewModel.handleIntent(UseCaseIntent.SetDomainPackage("/path/to/domain"))

            coEvery { mockGenerator(any()) } returns Result.success(Paths.get("out/UseCase.kt"))

            viewModel.handleIntent(UseCaseIntent.Generate)

            Then("success path should be set") {
                viewModel.state.test {
                    // Initial state (isGenerating = true)
                    val loadingState = awaitItem()
                    loadingState.isGenerating shouldBe true

                    testScope.advanceUntilIdle()

                    // Final state
                    val finalState = awaitItem()
                    finalState.isGenerating shouldBe false
                    finalState.lastGeneratedPath shouldBe "out/UseCase.kt"
                    finalState.errorMessage shouldBe null
                }
            }
        }

        When("Generate fails") {
            every { mockLoadRepositories(any()) } returns emptyList()
            viewModel.handleIntent(UseCaseIntent.SetName("ValidUseCase"))
            viewModel.handleIntent(UseCaseIntent.SetDomainPackage("/path/to/domain"))
            coEvery { mockGenerator(any()) } returns Result.failure(Exception("Failure"))

            viewModel.handleIntent(UseCaseIntent.Generate)

            Then("error message should be set") {
                viewModel.state.test {
                    // Initial state (isGenerating = true)
                    val loadingState = awaitItem()
                    loadingState.isGenerating shouldBe true

                    testScope.advanceUntilIdle()

                    // Final state
                    val finalState = awaitItem()
                    finalState.isGenerating shouldBe false
                    finalState.errorMessage shouldBe "Failure"
                }
            }
        }
    }
})
