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
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import com.jkjamies.cammp.domain.model.DatasourceStrategy
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.CleanArchitectureScaffoldRepositoryFake
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.file.Path

/**
 * Tests for [GenerateModulesStep].
 */
class GenerateModulesStepTest : BehaviorSpec({

    Given("GenerateModulesStep") {
        val params = CleanArchitectureParams(
            projectBasePath = Path.of("/project"),
            root = "feature",
            feature = "profile",
            orgCenter = "com.example",
            includePresentation = true,
            includeDiModule = true,
            datasourceStrategy = DatasourceStrategy.None,
            diStrategy = DiStrategy.Hilt,
        )

        When("scaffold repository succeeds") {
            Then("it should return StepResult.Scaffold with the generated result") {
                val expected = CleanArchitectureResult(
                    created = listOf("domain"),
                    skipped = listOf("data"),
                    settingsUpdated = false,
                    buildLogicCreated = false,
                    message = "ok",
                )

                val repo = CleanArchitectureScaffoldRepositoryFake(onGenerate = { expected })
                val step = GenerateModulesStep(repo)

                val result = step.execute(params)
                val scaffold = result.shouldBeInstanceOf<StepResult.Scaffold>()
                scaffold.result shouldBe expected

                repo.calls.single() shouldBe params
            }
        }

        When("scaffold repository throws") {
            Then("it should return StepResult.Failure") {
                val repo = CleanArchitectureScaffoldRepositoryFake(onGenerate = { throw IllegalStateException("boom") })
                val step = GenerateModulesStep(repo)

                val result = step.execute(params)
                result.shouldBeInstanceOf<StepResult.Failure>()
            }
        }
    }
})
