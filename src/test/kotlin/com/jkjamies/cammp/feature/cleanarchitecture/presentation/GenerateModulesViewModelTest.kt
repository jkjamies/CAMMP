package com.jkjamies.cammp.feature.cleanarchitecture.presentation

import app.cash.turbine.test
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import com.jkjamies.cammp.feature.cleanarchitecture.domain.usecase.CleanArchitectureGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

/**
 * Test class for [GenerateModulesViewModel].
 */
class GenerateModulesViewModelTest : BehaviorSpec({

    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)

    val mockGenerator = mockk<CleanArchitectureGenerator>()

    lateinit var viewModel: GenerateModulesViewModel

    beforeContainer {
        clearAllMocks()
        viewModel = GenerateModulesViewModel(
            projectBasePath = "project",
            scope = testScope,
            generator = mockGenerator
        )
    }

    afterSpec {
        unmockkAll()
    }

    Given("a GenerateModulesViewModel") {

        When("SetRoot intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetRoot("newRoot"))
            Then("state should update root") {
                viewModel.state.test {
                    awaitItem().root shouldBe "newRoot"
                }
            }
        }

        When("SetFeature intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetFeature("newFeature"))
            Then("state should update feature") {
                viewModel.state.test {
                    awaitItem().feature shouldBe "newFeature"
                }
            }
        }

        When("SetOrgCenter intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetOrgCenter("com.neworg"))
            Then("state should update orgCenter") {
                viewModel.state.test {
                    awaitItem().orgCenter shouldBe "com.neworg"
                }
            }
        }

        When("SetPlatformAndroid intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetPlatformAndroid(true))
            Then("state should update platformAndroid and platformKmp") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.platformAndroid shouldBe true
                    state.platformKmp shouldBe false
                }
            }
        }

        When("SetPlatformKmp intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetPlatformKmp(true))
            Then("state should update platformKmp and platformAndroid") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.platformKmp shouldBe true
                    state.platformAndroid shouldBe false
                }
            }
        }

        When("SetIncludePresentation intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetIncludePresentation(false))
            Then("state should update includePresentation") {
                viewModel.state.test {
                    awaitItem().includePresentation shouldBe false
                }
            }
        }

        When("SetIncludeDatasource intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetIncludeDatasource(false))
            Then("state should update includeDatasource") {
                viewModel.state.test {
                    awaitItem().includeDatasource shouldBe false
                }
            }
        }

        When("SetDatasourceCombined intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetDatasourceCombined(true))
            Then("state should update datasourceCombined") {
                viewModel.state.test {
                    awaitItem().datasourceCombined shouldBe true
                }
            }
        }

        When("SetDatasourceRemote intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetDatasourceRemote(true))
            Then("state should update datasourceRemote") {
                viewModel.state.test {
                    awaitItem().datasourceRemote shouldBe true
                }
            }
        }

        When("SetDatasourceLocal intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetDatasourceLocal(true))
            Then("state should update datasourceLocal") {
                viewModel.state.test {
                    awaitItem().datasourceLocal shouldBe true
                }
            }
        }

        When("SelectDiHilt intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SelectDiHilt(true))
            Then("state should update diHilt, diKoin, and diKoinAnnotations") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.diHilt shouldBe true
                    state.diKoin shouldBe false
                    state.diKoinAnnotations shouldBe false
                }
            }
        }

        When("SelectDiKoin intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SelectDiKoin(true))
            Then("state should update diKoin and diHilt") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.diKoin shouldBe true
                    state.diHilt shouldBe false
                }
            }
        }

        When("SetKoinAnnotations intent is handled") {
            viewModel.handleIntent(GenerateModulesIntent.SetKoinAnnotations(true))
            Then("state should update diKoinAnnotations") {
                viewModel.state.test {
                    awaitItem().diKoinAnnotations shouldBe true
                }
            }
        }

        When("Generate intent is handled with missing base path") {
            // Re-create VM with empty base path for this test case
            val vm = GenerateModulesViewModel(projectBasePath = "", scope = testScope, generator = mockGenerator)
            vm.handleIntent(GenerateModulesIntent.SetRoot(""))
            vm.handleIntent(GenerateModulesIntent.Generate)
            Then("state should show error") {
                vm.state.test {
                    awaitItem().errorMessage shouldBe "Project base path is required"
                }
            }
        }

        When("Generate intent is handled with KMP selected") {
            viewModel.handleIntent(GenerateModulesIntent.SetPlatformKmp(true))
            viewModel.handleIntent(GenerateModulesIntent.Generate)
            Then("state should show error") {
                viewModel.state.test {
                    awaitItem().errorMessage shouldBe "KMP generation is not supported yet in CAMMP"
                }
            }
        }

        When("Generate intent is handled with missing root or feature") {
            viewModel.handleIntent(GenerateModulesIntent.SetRoot(""))
            viewModel.handleIntent(GenerateModulesIntent.SetFeature(""))
            viewModel.handleIntent(GenerateModulesIntent.Generate)
            Then("state should show error") {
                viewModel.state.test {
                    awaitItem().errorMessage shouldBe "Root and Feature are required"
                }
            }
        }

        When("Generate intent is handled successfully") {
            val result = CleanArchitectureResult(
                created = listOf("domain", "data"),
                skipped = emptyList(),
                settingsUpdated = true,
                buildLogicCreated = true,
                message = "Success"
            )
            every { mockGenerator(any()) } returns Result.success(result)

            viewModel.handleIntent(GenerateModulesIntent.SetRoot("app"))
            viewModel.handleIntent(GenerateModulesIntent.SetFeature("feature"))
            viewModel.handleIntent(GenerateModulesIntent.SetPlatformAndroid(true))
            viewModel.handleIntent(GenerateModulesIntent.Generate)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("state should update with success") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.isGenerating shouldBe false
                    state.lastMessage shouldBe "Success"
                    state.lastCreated shouldBe listOf("domain", "data")
                    state.settingsUpdated shouldBe true
                    state.buildLogicCreated shouldBe true
                    state.errorMessage shouldBe null
                }
            }
            
            Then("generator should be called") {
                verify { mockGenerator(any()) }
            }
        }

        When("Generate intent is handled with failure") {
            every { mockGenerator(any()) } returns Result.failure(Exception("Generation failed"))

            viewModel.handleIntent(GenerateModulesIntent.SetRoot("app"))
            viewModel.handleIntent(GenerateModulesIntent.SetFeature("feature"))
            viewModel.handleIntent(GenerateModulesIntent.SetPlatformAndroid(true))
            viewModel.handleIntent(GenerateModulesIntent.Generate)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("state should show error") {
                viewModel.state.test {
                    val state = awaitItem()
                    state.isGenerating shouldBe false
                    state.errorMessage shouldBe "Generation failed"
                }
            }
        }
    }
})
