package com.jkjamies.cammp.feature.repositorygenerator.presentation

import app.cash.turbine.test
import com.jkjamies.cammp.feature.repositorygenerator.domain.usecase.LoadDataSourcesByType
import com.jkjamies.cammp.feature.repositorygenerator.domain.usecase.RepositoryGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

/**
 * Test class for [RepositoryViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
class RepositoryViewModelTest : BehaviorSpec({
    
    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)

    val mockGenerator = mockk<RepositoryGenerator>()
    val mockLoadDataSources = mockk<LoadDataSourcesByType>()

    lateinit var viewModel: RepositoryViewModel

    beforeContainer {
        clearAllMocks()
        viewModel = RepositoryViewModel(
            generator = mockGenerator,
            loadDataSourcesByType = mockLoadDataSources,
            scope = testScope
        )
    }

    afterSpec {
        unmockkAll()
    }

    Given("a repository view model") {

        When("initialized") {
            Then("state should be empty") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.name shouldBe ""
                    state.domainPackage shouldBe ""
                }
            }
        }

        When("repository name is set") {
            viewModel.handleIntent(RepositoryIntent.SetName("MyRepo"))
            Then("state should update repository name") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.name shouldBe "MyRepo"
                }
            }
        }

        When("package name is set with valid data module") {
            val path = "/path/to/data"
            every { mockLoadDataSources(path) } returns mapOf("Remote" to listOf("com.example.RemoteDS"))
            
            viewModel.handleIntent(RepositoryIntent.SetDomainPackage(path))
            
            Then("state should update package name and load datasources") {
                viewModel.state.test {
                    // Initial state has the path but empty datasources (before loading)
                    val loadingState = awaitItem()
                    loadingState.domainPackage shouldBe path

                    // Advance past the coroutine
                    testScope.advanceUntilIdle()

                    // Final state has loaded datasources
                    val finalState = awaitItem()
                    finalState.domainPackage shouldBe path
                    finalState.dataSourcesByType shouldBe mapOf("Remote" to listOf("com.example.RemoteDS"))
                }
            }
        }

        When("package name is set with invalid module") {
            val path = "/path/to/invalid"
            viewModel.handleIntent(RepositoryIntent.SetDomainPackage(path))
            
            Then("state should update package name but clear datasources") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.domainPackage shouldBe path
                    state.dataSourcesByType shouldBe emptyMap()
                }
            }
        }

        When("DI framework is toggled to Hilt") {
            viewModel.handleIntent(RepositoryIntent.SetDiHilt(true))
            Then("Koin should be disabled and Hilt enabled") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.diKoin shouldBe false
                    state.diHilt shouldBe true
                }
            }
        }

        When("DI framework is toggled to Koin") {
            viewModel.handleIntent(RepositoryIntent.SetDiKoin(true))
            Then("Hilt should be disabled and Koin enabled") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.diHilt shouldBe false
                    state.diKoin shouldBe true
                }
            }
        }

        When("Koin annotations are toggled") {
            viewModel.handleIntent(RepositoryIntent.SetDiKoin(true))
            viewModel.handleIntent(RepositoryIntent.ToggleKoinAnnotations(true))
            Then("state should reflect the change") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.diHilt shouldBe false
                    state.diKoin shouldBe true
                    state.diKoinAnnotations shouldBe true
                }
            }
        }

        When("Generate implementation is toggled") {
            viewModel.handleIntent(RepositoryIntent.SetGenerateImplementation(false))
            Then("state should reflect the change") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.generateImplementation shouldBe false
                }
            }
        }
        
        When("Include datasource is toggled") {
            viewModel.handleIntent(RepositoryIntent.SetIncludeDatasource(true))
            Then("state should reflect the change") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.includeDatasource shouldBe true
                }
            }
        }

        When("Datasource combined is toggled") {
            viewModel.handleIntent(RepositoryIntent.SetDatasourceCombined(true))
            Then("Combined should be true, others false") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.datasourceCombined shouldBe true
                    state.datasourceRemote shouldBe false
                    state.datasourceLocal shouldBe false
                }
            }
        }

        When("Datasource remote is toggled") {
            viewModel.handleIntent(RepositoryIntent.SetDatasourceRemote(true))
            Then("Remote should be true, combined false") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.datasourceRemote shouldBe true
                    state.datasourceCombined shouldBe false
                }
            }
        }

        When("Datasource local is toggled") {
            viewModel.handleIntent(RepositoryIntent.SetDatasourceLocal(true))
            Then("Local should be true, combined false") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.datasourceLocal shouldBe true
                    state.datasourceCombined shouldBe false
                }
            }
        }

        When("Generate is called with invalid state") {
            viewModel.handleIntent(RepositoryIntent.SetName("")) // Invalid name
            viewModel.handleIntent(RepositoryIntent.Generate)
            Then("error message should be set") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.errorMessage shouldBe "Name is required"
                }
            }
        }

        When("Generate is called with valid state") {
            every { mockLoadDataSources(any()) } returns emptyMap()
            viewModel.handleIntent(RepositoryIntent.SetName("ValidRepo"))
            viewModel.handleIntent(RepositoryIntent.SetDomainPackage("/path/to/data"))
            
            coEvery { mockGenerator(any()) } returns Result.success("Success")
            
            viewModel.handleIntent(RepositoryIntent.Generate)
            
            Then("success message should be set") {
                viewModel.state.test {
                    // Initial state (isGenerating = true)
                    val loadingState = awaitItem()
                    loadingState.isGenerating shouldBe true

                    testScope.advanceUntilIdle()

                    // Final state
                    val finalState = awaitItem()
                    finalState.isGenerating shouldBe false
                    finalState.lastGeneratedMessage shouldBe "Success"
                    finalState.errorMessage shouldBe null
                }
            }
        }

        When("Generate fails") {
            every { mockLoadDataSources(any()) } returns emptyMap()
            viewModel.handleIntent(RepositoryIntent.SetName("ValidRepo"))
            viewModel.handleIntent(RepositoryIntent.SetDomainPackage("/path/to/data"))
            coEvery { mockGenerator(any()) } returns Result.failure(Exception("Failure"))
            
            viewModel.handleIntent(RepositoryIntent.Generate)
            
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
