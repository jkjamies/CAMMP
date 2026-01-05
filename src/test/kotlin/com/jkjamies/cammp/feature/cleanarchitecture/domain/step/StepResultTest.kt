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
