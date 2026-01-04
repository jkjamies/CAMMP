package com.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.step.StepResult
import com.jkjamies.cammp.feature.usecasegenerator.testutil.UseCaseStepFake
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

/**
 * Tests for [UseCaseGenerator].
 */
class UseCaseGeneratorTest : BehaviorSpec({

    Given("a UseCaseGenerator") {
        val params = UseCaseParams(
            domainDir = Paths.get("domain"),
            className = "Test",
            diStrategy = DiStrategy.Hilt,
            repositories = emptyList(),
        )

        When("all steps succeed and one returns a path") {
            Then("it should return success and normalize className passed to steps") {
                val step1 = UseCaseStepFake(ArrayDeque(listOf(StepResult.Success(null))))
                val step2 = UseCaseStepFake(ArrayDeque(listOf(StepResult.Success(Paths.get("domain/TestUseCase.kt")))))
                val generator = UseCaseGenerator(setOf(step1, step2))

                val result = generator(params)

                result.shouldBeSuccess { it shouldBe Paths.get("domain/TestUseCase.kt") }

                step1.calls.single().className shouldBe "TestUseCase"
                step2.calls.single().className shouldBe "TestUseCase"
            }
        }

        When("steps succeed but no path is returned") {
            Then("it should return failure") {
                val step1 = UseCaseStepFake(ArrayDeque(listOf(StepResult.Success(null))))
                val step2 = UseCaseStepFake(ArrayDeque(listOf(StepResult.Success(null))))
                val generator = UseCaseGenerator(setOf(step1, step2))

                val result = generator(params)
                result.shouldBeFailure<IllegalStateException>()
            }
        }

        When("a step fails") {
            Then("it should return failure with the original error") {
                val error = RuntimeException("Boom")
                val step1 = UseCaseStepFake(ArrayDeque(listOf(StepResult.Failure(error))))
                val step2 = UseCaseStepFake()
                val generator = UseCaseGenerator(setOf(step1, step2))

                val result = generator(params)
                result.shouldBeFailure { it shouldBe error }
            }
        }
    }
})