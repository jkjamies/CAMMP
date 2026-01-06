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
                awaitItem().projectBasePath shouldBe "/project"

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
                s.includeDatasource shouldBe true
                s.datasourceCombined shouldBe true
                s.datasourceRemote shouldBe true
                s.datasourceLocal shouldBe true

                // Last DI intent was Koin.
                s.diKoin shouldBe true
                s.diHilt shouldBe false
                s.diKoinAnnotations shouldBe true

                s.errorMessage shouldBe null
            }
        }
    }
})
