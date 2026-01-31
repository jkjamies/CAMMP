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

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class StepResultTest : BehaviorSpec({

    Given("StepResult") {
        When("each result type is created") {
            Then("they should retain their default and provided values") {
                StepResult.Success().message shouldBe ""
                StepResult.Success("ok").message shouldBe "ok"

                StepResult.Settings(updated = true).updated shouldBe true
                StepResult.Settings(false, "settings").message shouldBe "settings"

                StepResult.BuildLogic(updated = true).updated shouldBe true
                StepResult.BuildLogic(false, "buildLogic").message shouldBe "buildLogic"

                val result = CleanArchitectureResult(
                    created = emptyList(),
                    skipped = emptyList(),
                    settingsUpdated = false,
                    buildLogicCreated = false,
                    message = "done",
                )
                StepResult.Scaffold(result).result shouldBe result

                val err = IllegalStateException("boom")
                StepResult.Failure(err).error shouldBe err
            }
        }
    }
})
