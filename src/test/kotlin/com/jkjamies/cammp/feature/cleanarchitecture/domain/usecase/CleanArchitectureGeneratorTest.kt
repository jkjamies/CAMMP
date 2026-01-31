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

package com.jkjamies.cammp.feature.cleanarchitecture.domain.usecase

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.step.CleanArchitectureStep
import com.jkjamies.cammp.feature.cleanarchitecture.domain.step.StepResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import java.nio.file.Path

/**
 * Tests for [CleanArchitectureGenerator].
 */
class CleanArchitectureGeneratorTest : BehaviorSpec({

    Given("CleanArchitectureGenerator") {

        val params = CleanArchitectureParams(
            projectBasePath = Path.of("/project"),
            root = "feature",
            feature = "profile",
            orgCenter = "com.example",
            includePresentation = true,
            includeDiModule = true,
            datasourceStrategy = DatasourceStrategy.Combined,
        )

        When("a step fails") {
            val failingStep = object : CleanArchitectureStep {
                override suspend fun execute(params: CleanArchitectureParams): StepResult =
                    StepResult.Failure(IllegalStateException("boom"))
            }
            val scaffoldStep = object : CleanArchitectureStep {
                override suspend fun execute(params: CleanArchitectureParams): StepResult =
                    StepResult.Scaffold(
                        CleanArchitectureResult(
                            created = listOf("domain"),
                            skipped = emptyList(),
                            settingsUpdated = false,
                            buildLogicCreated = false,
                            message = "ok",
                        )
                    )
            }

            val gen = CleanArchitectureGenerator(setOf(scaffoldStep, failingStep))

            Then("invoke should return failure") {
                gen.invoke(params).shouldBeFailure()
            }
        }

        When("scaffold step is missing") {
            val okStep = object : CleanArchitectureStep {
                override suspend fun execute(params: CleanArchitectureParams): StepResult =
                    StepResult.Success("ok")
            }

            val gen = CleanArchitectureGenerator(setOf(okStep))

            Then("invoke should return failure") {
                gen.invoke(params).shouldBeFailure()
            }
        }

        When("steps provide scaffold + settings/buildlogic") {
            val scaffoldStep = object : CleanArchitectureStep {
                override suspend fun execute(params: CleanArchitectureParams): StepResult =
                    StepResult.Scaffold(
                        CleanArchitectureResult(
                            created = listOf("domain"),
                            skipped = emptyList(),
                            settingsUpdated = false,
                            buildLogicCreated = false,
                            message = "",
                        )
                    )
            }
            val settingsStep = object : CleanArchitectureStep {
                override suspend fun execute(params: CleanArchitectureParams): StepResult =
                    StepResult.Settings(updated = true, message = "settings")
            }
            val buildLogicStep = object : CleanArchitectureStep {
                override suspend fun execute(params: CleanArchitectureParams): StepResult =
                    StepResult.BuildLogic(updated = true, message = "buildlogic")
            }

            val gen = CleanArchitectureGenerator(setOf(scaffoldStep, settingsStep, buildLogicStep))

            Then("it should aggregate flags into the final scaffold result") {
                val r = gen.invoke(params).shouldBeSuccess()
                r.created shouldBe listOf("domain")
                r.settingsUpdated shouldBe true
                r.buildLogicCreated shouldBe true
            }
        }
    }
})
