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

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.domain.model.DatasourceStrategy
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.GradleSettingsScaffoldRepositoryFake
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.nio.file.Path

/**
 * Tests for [UpdateSettingsStep].
 */
class UpdateSettingsStepTest : BehaviorSpec({

    Given("UpdateSettingsStep") {
        val params = CleanArchitectureParams(
            projectBasePath = Path.of("/project"),
            root = "app",
            feature = "my-feature",
            orgCenter = "com.example",
            includePresentation = true,
            includeDiModule = true,
            datasourceStrategy = DatasourceStrategy.RemoteOnly,
            diStrategy = DiStrategy.Koin(useAnnotations = true),
        )

        When("executed") {
            val repo = GradleSettingsScaffoldRepositoryFake(updated = true)
            val step = UpdateSettingsStep(repo)

            val result = step.execute(params)

            Then("it should return StepResult.Settings reflecting updated=true") {
                result shouldBe StepResult.Settings(updated = true, message = "- settings updated: true")
            }

            Then("it should compute enabled module list") {
                repo.calls.single().enabledModules.shouldContainExactly(
                    "domain",
                    "data",
                    "di",
                    "presentation",
                    "remoteDataSource",
                )
            }

            Then("it should compute di mode") {
                repo.calls.single().diMode shouldBe DiMode.KOIN_ANNOTATIONS
            }
        }

        When("executed with includeApiModule = true") {
            val repo = GradleSettingsScaffoldRepositoryFake(updated = false)
            val step = UpdateSettingsStep(repo)

            step.execute(params.copy(includeApiModule = true))

            Then("it should compute enabled module list with api") {
                repo.calls.single().enabledModules.shouldContainExactly(
                    "domain",
                    "data",
                    "api",
                    "di",
                    "presentation",
                    "remoteDataSource",
                )
            }
        }

        When("datasourceStrategy is LocalOnly") {
            Then("it should include localDataSource in enabled modules") {
                val localParams = params.copy(
                    datasourceStrategy = DatasourceStrategy.LocalOnly,
                    includePresentation = false,
                )

                val repo = GradleSettingsScaffoldRepositoryFake(updated = false)
                val step = UpdateSettingsStep(repo)

                step.execute(localParams) shouldBe StepResult.Settings(updated = false, message = "- settings updated: false")

                repo.calls.single().enabledModules.shouldContainExactly(
                    "domain",
                    "data",
                    "di",
                    "localDataSource",
                )
            }
        }

        When("executed with includeDiModule = false") {
            val repo = GradleSettingsScaffoldRepositoryFake(updated = false)
            val step = UpdateSettingsStep(repo)

            step.execute(params.copy(includeDiModule = false))

            Then("it should compute enabled module list without di") {
                repo.calls.single().enabledModules.shouldContainExactly(
                    "domain",
                    "data",
                    "presentation",
                    "remoteDataSource",
                )
            }
        }
    }
})
