package com.jkjamies.cammp.feature.repositorygenerator.presentation

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.usecase.LoadDataSourcesByType
import com.jkjamies.cammp.feature.repositorygenerator.domain.usecase.RepositoryGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.TestScope

class RepositoryViewModelTest : BehaviorSpec({

    val generator = mockk<RepositoryGenerator>()
    val loadDataSources = mockk<LoadDataSourcesByType>()
    val testScope = TestScope()

    Given("RepositoryViewModel") {
        val viewModel = RepositoryViewModel(
            domainPackage = "",
            scope = testScope,
            generator = generator,
            loadDataSourcesByType = loadDataSources
        )

        When("SetDomainPackage is called") {
            val path = "/project/feature/data"
            every { loadDataSources(path) } returns mapOf("Remote" to listOf("com.example.RemoteDS"))
            
            viewModel.handleIntent(RepositoryIntent.SetDomainPackage(path))
            testScope.testScheduler.advanceUntilIdle()

            Then("state should update and load data sources") {
                viewModel.state.value.domainPackage shouldBe path
                viewModel.state.value.dataSourcesByType shouldBe mapOf("Remote" to listOf("com.example.RemoteDS"))
            }
        }

        When("Generate is called with invalid input") {
            viewModel.handleIntent(RepositoryIntent.SetName(""))
            viewModel.handleIntent(RepositoryIntent.Generate)

            Then("errorMessage should be set") {
                viewModel.state.value.errorMessage shouldBe "Name is required"
            }
        }

        When("Generate is called with valid input") {
            viewModel.handleIntent(RepositoryIntent.SetDomainPackage("/project/feature/data"))
            viewModel.handleIntent(RepositoryIntent.SetName("UserRepository"))
            viewModel.handleIntent(RepositoryIntent.SetDiHilt(true))
            
            val slot = slot<RepositoryParams>()
            coEvery { generator(capture(slot)) } returns Result.success("Success")

            viewModel.handleIntent(RepositoryIntent.Generate)
            testScope.testScheduler.advanceUntilIdle()

            Then("generator should be called with correct params") {
                slot.captured.className shouldBe "UserRepository"
                slot.captured.dataDir.toString() shouldBe "/project/feature/data"
                viewModel.state.value.lastGeneratedMessage shouldBe "Success"
                viewModel.state.value.errorMessage shouldBe null
            }
        }

        When("Toggling Datasource options") {
            viewModel.handleIntent(RepositoryIntent.SetIncludeDatasource(true))
            
            And("Combined is selected") {
                viewModel.handleIntent(RepositoryIntent.SetDatasourceCombined(true))
                
                Then("Remote and Local should be deselected") {
                    val s = viewModel.state.value
                    s.datasourceCombined shouldBe true
                    s.datasourceRemote shouldBe false
                    s.datasourceLocal shouldBe false
                }
            }

            And("Remote is selected") {
                viewModel.handleIntent(RepositoryIntent.SetDatasourceRemote(true))
                
                Then("Combined should be deselected") {
                    val s = viewModel.state.value
                    s.datasourceRemote shouldBe true
                    s.datasourceCombined shouldBe false
                }
            }
        }

        When("Validating Data Path") {
            viewModel.handleIntent(RepositoryIntent.SetDomainPackage("/project/feature/domain")) // Invalid, should be data
            viewModel.handleIntent(RepositoryIntent.Generate)
            
            Then("it should error if not a data module") {
                viewModel.state.value.errorMessage shouldContain "must be a data module"
            }
        }
    }
})