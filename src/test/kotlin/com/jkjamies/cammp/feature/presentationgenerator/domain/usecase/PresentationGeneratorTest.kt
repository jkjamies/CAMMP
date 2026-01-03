package com.jkjamies.cammp.feature.presentationgenerator.domain.usecase

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.step.PresentationStep
import com.jkjamies.cammp.feature.presentationgenerator.domain.step.StepResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Paths

/**
 * Test class for [PresentationGenerator].
 */
class PresentationGeneratorTest : BehaviorSpec({

    val step1 = mockk<PresentationStep>()
    val step2 = mockk<PresentationStep>()
    val steps = setOf(step1, step2)
    val generator = PresentationGenerator(steps)

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a presentation generator") {
        val params = PresentationParams(
            moduleDir = Paths.get("/fake/path"),
            screenName = "TestScreen",
            patternStrategy = PresentationPatternStrategy.MVVM,
            diStrategy = DiStrategy.Hilt
        )

        When("invoking the generator") {
            coEvery { step1.execute(params) } returns StepResult.Success("Step 1 done")
            coEvery { step2.execute(params) } returns StepResult.Success("Step 2 done")

            val result = generator(params)

            Then("it should execute all steps") {
                coVerify(exactly = 1) { step1.execute(params) }
                coVerify(exactly = 1) { step2.execute(params) }
            }

            Then("it should return a success result with messages") {
                result.shouldBeSuccess()
                val message = result.getOrNull() ?: ""
                message shouldContain "Step 1 done"
                message shouldContain "Step 2 done"
            }
        }

        When("a step fails") {
            val error = RuntimeException("Step failed")
            coEvery { step1.execute(params) } returns StepResult.Success("Step 1 done")
            coEvery { step2.execute(params) } returns StepResult.Failure(error)

            val result = generator(params)

            Then("it should return a failure result") {
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe error
            }
        }
    }
})
