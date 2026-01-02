package com.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.step.StepResult
import com.jkjamies.cammp.feature.usecasegenerator.domain.step.UseCaseStep
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import java.nio.file.Paths

class UseCaseGeneratorTest : BehaviorSpec({

    val step1 = mockk<UseCaseStep>()
    val step2 = mockk<UseCaseStep>()
    val steps = setOf(step1, step2)
    val generator = UseCaseGenerator(steps)

    val params = UseCaseParams(
        domainDir = Paths.get("domain"),
        className = "Test",
        diStrategy = DiStrategy.Hilt
    )

    Given("A UseCaseGenerator with steps") {

        When("All steps succeed and return a path") {
            val expectedPath = Paths.get("domain/TestUseCase.kt")
            
            val slot1 = slot<UseCaseParams>()
            val slot2 = slot<UseCaseParams>()

            coEvery { step1.execute(capture(slot1)) } returns StepResult.Success(null)
            coEvery { step2.execute(capture(slot2)) } returns StepResult.Success(expectedPath)

            val result = generator(params)

            Then("Result should be success with correct path") {
                result.shouldBeSuccess {
                    it shouldBe expectedPath
                }
            }

            Then("Params passed to steps should have normalized class name") {
                slot1.captured.className shouldBe "TestUseCase"
                slot2.captured.className shouldBe "TestUseCase"
            }
        }

        When("Steps succeed but no path returned") {
            coEvery { step1.execute(any()) } returns StepResult.Success(null)
            coEvery { step2.execute(any()) } returns StepResult.Success(null)

            val result = generator(params)

            Then("Result should be failure with IllegalStateException") {
                result.shouldBeFailure<IllegalStateException>()
            }
        }

        When("A step fails") {
            val error = RuntimeException("Boom")
            coEvery { step1.execute(any()) } returns StepResult.Failure(error)
            
            val result = generator(params)

            Then("Result should be failure") {
                result.shouldBeFailure {
                    it shouldBe error
                }
            }
        }
    }
})