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

package com.jkjamies.cammp.feature.repositorygenerator.presentation

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.usecase.LoadDataSourcesByType
import com.jkjamies.cammp.feature.repositorygenerator.domain.usecase.RepositoryGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.TestScope

/**
 * Tests for [RepositoryViewModel].
 */
class RepositoryViewModelTest : BehaviorSpec({

    data class Harness(
        val scope: TestScope = TestScope(),
        val generator: RepositoryGenerator,
        val loadDataSources: LoadDataSourcesByType,
        val vm: RepositoryViewModel,
    )

    fun newHarness(domainPackage: String = ""): Harness {
        val scope = TestScope()
        val generator = mockk<RepositoryGenerator>()
        val loadDataSources = mockk<LoadDataSourcesByType>()
        val vm = RepositoryViewModel(
            domainPackage = domainPackage,
            scope = scope,
            generator = generator,
            loadDataSourcesByType = loadDataSources,
        )
        return Harness(scope, generator, loadDataSources, vm)
    }

    Given("RepositoryViewModel") {

        When("SetDomainPackage is called for a valid data module") {
            Then("it should update the path, load data sources, and filter selected items") {
                val (scope, _, loadDataSources, vm) = newHarness()

                // Preselect a datasource that will become invalid after reload.
                vm.handleIntent(RepositoryIntent.ToggleDataSourceSelection("com.example.Invalid", selected = true))

                val path = "/project/feature/data"
                val map = mapOf(
                    "Remote" to listOf("com.example.RemoteDS"),
                    "Local" to listOf("com.example.LocalDS"),
                )
                every { loadDataSources(path) } returns map

                vm.handleIntent(RepositoryIntent.SetDomainPackage(path))
                scope.testScheduler.advanceUntilIdle()

                vm.state.value.domainPackage shouldBe path
                vm.state.value.dataSourcesByType shouldBe map

                // Invalid selection should be filtered out.
                vm.state.value.selectedDataSources.shouldBeEmpty()
            }
        }

        When("SetDomainPackage is called for an invalid (non-data) path") {
            Then("it should clear data sources and selections") {
                val (_, _, _, vm) = newHarness()

                vm.handleIntent(RepositoryIntent.ToggleDataSourceSelection("com.example.RemoteDS", selected = true))

                vm.handleIntent(RepositoryIntent.SetDomainPackage("/project/feature/domain"))
                vm.handleIntent(RepositoryIntent.SetName("UserRepository"))
                vm.handleIntent(RepositoryIntent.Generate)

                vm.state.value.errorMessage shouldContain "must be a data module"
            }
        }

        When("Generate is called with invalid input") {
            Then("it should set an error message") {
                val (_, _, _, vm) = newHarness()

                vm.handleIntent(RepositoryIntent.SetName(""))
                vm.handleIntent(RepositoryIntent.Generate)

                vm.state.value.errorMessage shouldBe "Name is required"
            }
        }

        When("Generate is called with a non-data module path") {
            Then("it should error") {
                val (_, _, _, vm) = newHarness()

                vm.handleIntent(RepositoryIntent.SetDomainPackage("/project/feature/domain"))
                vm.handleIntent(RepositoryIntent.SetName("UserRepository"))
                vm.handleIntent(RepositoryIntent.Generate)

                vm.state.value.errorMessage shouldContain "must be a data module"
            }
        }

        When("Generate is called with valid input (Metro default)") {
            Then("it should call generator with correct params and update lastGeneratedMessage") {
                val (scope, generator, loadDataSources, vm) = newHarness()

                val path = "/project/feature/data"
                val dsMap = mapOf("Remote" to listOf("com.example.RemoteDS"))
                every { loadDataSources(path) } returns dsMap

                vm.handleIntent(RepositoryIntent.SetDomainPackage(path))
                scope.testScheduler.advanceUntilIdle()

                vm.handleIntent(RepositoryIntent.SetName("UserRepository"))

                vm.handleIntent(RepositoryIntent.SetIncludeDatasource(true))
                vm.handleIntent(RepositoryIntent.SetDatasourceCombined(true))
                vm.handleIntent(RepositoryIntent.SetDatasourceRemote(false))
                vm.handleIntent(RepositoryIntent.SetDatasourceLocal(false))

                vm.handleIntent(RepositoryIntent.SetDataSourcesByType(dsMap))
                vm.handleIntent(RepositoryIntent.ToggleDataSourceSelection("com.example.RemoteDS", selected = true))

                coEvery { generator(any()) } returns Result.success("Success")

                vm.handleIntent(RepositoryIntent.Generate)
                scope.testScheduler.advanceUntilIdle()

                val paramsSlot = slot<RepositoryParams>()
                coVerify(exactly = 1) { generator(capture(paramsSlot)) }

                val captured = paramsSlot.captured
                captured.className shouldBe "UserRepository"
                captured.dataDir.toString() shouldBe "/project/feature/data"
                captured.diStrategy shouldBe DiStrategy.Metro
                captured.datasourceStrategy shouldBe DatasourceStrategy.Combined
                captured.selectedDataSources.shouldContainExactly("com.example.RemoteDS")

                vm.state.value.lastGeneratedMessage shouldBe "Success"
                vm.state.value.errorMessage shouldBe null
                vm.state.value.isGenerating shouldBe false
            }
        }

        When("Toggling datasource options") {
            Then("it should enforce mutual exclusivity rules") {
                val (_, _, _, vm) = newHarness()

                vm.handleIntent(RepositoryIntent.SetIncludeDatasource(true))
                vm.handleIntent(RepositoryIntent.SetDatasourceCombined(true))

                val s1 = vm.state.value
                s1.datasourceCombined shouldBe true
                s1.datasourceRemote shouldBe false
                s1.datasourceLocal shouldBe false

                vm.handleIntent(RepositoryIntent.SetDatasourceRemote(true))

                val s2 = vm.state.value
                s2.datasourceRemote shouldBe true
                s2.datasourceCombined shouldBe false

                vm.handleIntent(RepositoryIntent.SetDatasourceLocal(true))

                val s3 = vm.state.value
                s3.datasourceCombined shouldBe false
                s3.datasourceRemote shouldBe true
                s3.datasourceLocal shouldBe true
            }
        }

        When("switching DI between Metro, Hilt and Koin") {
            Then("it should keep the flags consistent") {
                val (_, _, _, vm) = newHarness()

                vm.state.value.diMetro shouldBe true
                vm.state.value.diHilt shouldBe false
                vm.state.value.diKoin shouldBe false

                vm.handleIntent(RepositoryIntent.SetDiKoin(true))
                vm.state.value.diKoin shouldBe true
                vm.state.value.diMetro shouldBe false
                vm.state.value.diHilt shouldBe false

                vm.handleIntent(RepositoryIntent.ToggleKoinAnnotations(true))
                vm.state.value.diKoinAnnotations shouldBe true

                vm.handleIntent(RepositoryIntent.SetDiMetro(true))
                vm.state.value.diMetro shouldBe true
                vm.state.value.diHilt shouldBe false
                vm.state.value.diKoin shouldBe false
                vm.state.value.diKoinAnnotations shouldBe false

                vm.handleIntent(RepositoryIntent.SetDiHilt(true))
                vm.state.value.diHilt shouldBe true
                vm.state.value.diMetro shouldBe false
                vm.state.value.diKoin shouldBe false
                // Hilt forces annotations off
                vm.state.value.diKoinAnnotations shouldBe false
            }
        }

    }
})
