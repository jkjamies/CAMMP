package com.jkjamies.cammp.feature.repositorygenerator.presentation

import app.cash.turbine.test
import com.jkjamies.cammp.feature.repositorygenerator.domain.usecase.LoadDataSourcesByType
import com.jkjamies.cammp.feature.repositorygenerator.domain.usecase.RepositoryGenerator
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryViewModelTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    Given("a repository view model") {
        val mockGenerator = mockk<RepositoryGenerator>()
        val mockLoadDataSources = mockk<LoadDataSourcesByType>()
        val viewModel = RepositoryViewModel(
            generator = mockGenerator,
            loadDataSourcesByType = mockLoadDataSources
        )

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
                    // Skip initial state if needed, but here we expect the update
                    // Since flow is hot, we might miss emission if not careful, but turbine usually catches up
                    // However, with StandardTestDispatcher, we need to run current
                    testDispatcher.scheduler.runCurrent()
                    val state = awaitItem()
                    state.domainPackage shouldBe path
                    state.dataSourcesByType shouldBe mapOf("Remote" to listOf("com.example.RemoteDS"))
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
            viewModel.handleIntent(RepositoryIntent.SetName("ValidRepo"))
            viewModel.handleIntent(RepositoryIntent.SetDomainPackage("/path/to/data"))
            
            coEvery { mockGenerator(any()) } returns Result.success("Success")
            
            viewModel.handleIntent(RepositoryIntent.Generate)
            
            Then("success message should be set") {
                viewModel.state.test {
                    testDispatcher.scheduler.runCurrent()
                    val state = awaitItem()
                    state.lastGeneratedMessage shouldBe "Success"
                    state.errorMessage shouldBe null
                }
            }
        }

        When("Generate fails") {
            viewModel.handleIntent(RepositoryIntent.SetName("ValidRepo"))
            viewModel.handleIntent(RepositoryIntent.SetDomainPackage("/path/to/data"))
            coEvery { mockGenerator(any()) } returns Result.failure(Exception("Failure"))
            
            viewModel.handleIntent(RepositoryIntent.Generate)
            
            Then("error message should be set") {
                viewModel.state.test {
                    testDispatcher.scheduler.runCurrent()
                    val state = awaitItem()
                    state.errorMessage shouldBe "Failure"
                }
            }
        }
    }
})
