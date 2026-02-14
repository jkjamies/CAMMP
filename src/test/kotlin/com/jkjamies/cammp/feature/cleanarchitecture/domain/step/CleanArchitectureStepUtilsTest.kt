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

package com.jkjamies.cammp.feature.cleanarchitecture.domain.step

import com.jkjamies.cammp.domain.model.DatasourceStrategy
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.nio.file.Path

/**
 * Tests for [resolveEnabledModules] and [resolveDiMode].
 */
class CleanArchitectureStepUtilsTest : BehaviorSpec({

    fun baseParams(
        includePresentation: Boolean = false,
        includeApiModule: Boolean = false,
        includeDiModule: Boolean = false,
        datasourceStrategy: DatasourceStrategy = DatasourceStrategy.None,
        diStrategy: DiStrategy = DiStrategy.Hilt,
    ) = CleanArchitectureParams(
        projectBasePath = Path.of("/project"),
        root = "app",
        feature = "my-feature",
        orgCenter = "com.example",
        includePresentation = includePresentation,
        includeApiModule = includeApiModule,
        includeDiModule = includeDiModule,
        datasourceStrategy = datasourceStrategy,
        diStrategy = diStrategy,
    )

    Given("resolveEnabledModules") {
        When("minimal params with no optional modules") {
            Then("it should return domain and data only") {
                resolveEnabledModules(baseParams()) shouldContainExactly listOf("domain", "data")
            }
        }

        When("all optional modules enabled with Combined datasource") {
            Then("it should include all modules") {
                resolveEnabledModules(
                    baseParams(
                        includePresentation = true,
                        includeApiModule = true,
                        includeDiModule = true,
                        datasourceStrategy = DatasourceStrategy.Combined,
                    )
                ) shouldContainExactly listOf("domain", "data", "api", "di", "presentation", "dataSource")
            }
        }

        When("RemoteOnly datasource") {
            Then("it should include remoteDataSource") {
                resolveEnabledModules(
                    baseParams(datasourceStrategy = DatasourceStrategy.RemoteOnly)
                ) shouldContainExactly listOf("domain", "data", "remoteDataSource")
            }
        }

        When("LocalOnly datasource") {
            Then("it should include localDataSource") {
                resolveEnabledModules(
                    baseParams(datasourceStrategy = DatasourceStrategy.LocalOnly)
                ) shouldContainExactly listOf("domain", "data", "localDataSource")
            }
        }

        When("RemoteAndLocal datasource") {
            Then("it should include both remote and local") {
                resolveEnabledModules(
                    baseParams(datasourceStrategy = DatasourceStrategy.RemoteAndLocal)
                ) shouldContainExactly listOf("domain", "data", "remoteDataSource", "localDataSource")
            }
        }
    }

    Given("resolveDiMode") {
        When("strategy is Hilt") {
            Then("it should return HILT") {
                resolveDiMode(baseParams(diStrategy = DiStrategy.Hilt)) shouldBe DiMode.HILT
            }
        }

        When("strategy is Metro") {
            Then("it should return METRO") {
                resolveDiMode(baseParams(diStrategy = DiStrategy.Metro)) shouldBe DiMode.METRO
            }
        }

        When("strategy is Koin with annotations") {
            Then("it should return KOIN_ANNOTATIONS") {
                resolveDiMode(baseParams(diStrategy = DiStrategy.Koin(useAnnotations = true))) shouldBe DiMode.KOIN_ANNOTATIONS
            }
        }

        When("strategy is Koin without annotations") {
            Then("it should return KOIN") {
                resolveDiMode(baseParams(diStrategy = DiStrategy.Koin(useAnnotations = false))) shouldBe DiMode.KOIN
            }
        }
    }

    Given("runCleanArchStepCatching") {
        When("block succeeds") {
            Then("it should return the result") {
                val result = runCleanArchStepCatching { StepResult.Success("ok") }
                result shouldBe StepResult.Success("ok")
            }
        }

        When("block throws") {
            Then("it should return Failure wrapping the exception") {
                val error = IllegalStateException("boom")
                val result = runCleanArchStepCatching { throw error }
                result shouldBe StepResult.Failure(error)
            }
        }
    }
})
