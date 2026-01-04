package com.jkjamies.cammp.feature.presentationgenerator.domain.usecase

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.step.PresentationStep
import com.jkjamies.cammp.feature.presentationgenerator.domain.step.StepResult
import com.jkjamies.cammp.feature.presentationgenerator.testutil.TestFiles
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path

/**
 * Tests for [PresentationGenerator].
 */
class PresentationGeneratorTest : BehaviorSpec({

    fun params(moduleDir: Path) = PresentationParams(
        moduleDir = moduleDir,
        screenName = "Home",
        patternStrategy = PresentationPatternStrategy.MVI,
        diStrategy = DiStrategy.Hilt,
    )

    Given("PresentationGenerator") {

        When("all steps succeed") {
            Then("it should return title + non-null messages") {
                TestFiles.withTempDir("pg_usecase") { dir ->
                    val steps = linkedSetOf(
                        object : PresentationStep {
                            override suspend fun execute(params: PresentationParams): StepResult =
                                StepResult.Success("- A")
                        },
                        object : PresentationStep {
                            override suspend fun execute(params: PresentationParams): StepResult =
                                StepResult.Success(null)
                        },
                        object : PresentationStep {
                            override suspend fun execute(params: PresentationParams): StepResult =
                                StepResult.Success("- B")
                        }
                    )

                    val generator = PresentationGenerator(steps)

                    val result = generator(params(dir))
                    result.isSuccess shouldBe true
                    result.getOrThrow() shouldBe "Presentation generation completed:\n- A\n- B"
                }
            }
        }

        When("a step fails") {
            Then("it should return a failure") {
                TestFiles.withTempDir("pg_usecase") { dir ->
                    val boom = IllegalStateException("boom")

                    val steps = linkedSetOf(
                        object : PresentationStep {
                            override suspend fun execute(params: PresentationParams): StepResult = StepResult.Success("- A")
                        },
                        object : PresentationStep {
                            override suspend fun execute(params: PresentationParams): StepResult = StepResult.Failure(boom)
                        }
                    )

                    val generator = PresentationGenerator(steps)

                    val result = generator(params(dir))
                    result.isFailure shouldBe true
                    result.exceptionOrNull() shouldBe boom
                }
            }
        }
    }
})

