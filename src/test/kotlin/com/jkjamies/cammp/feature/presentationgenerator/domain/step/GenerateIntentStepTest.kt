package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.IntentRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Test for [GenerateIntentStep].
 */
class GenerateIntentStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val intentRepo = mockk<IntentRepository>()
    val step = GenerateIntentStep(modulePkgRepo, intentRepo)

    Given("GenerateIntentStep") {
        val tempDir = Files.createTempDirectory("intent_step_test")
        val moduleDir = tempDir.resolve("feature")
        Files.createDirectories(moduleDir)

        val params = PresentationParams(
            moduleDir = moduleDir,
            screenName = "Test",
            patternStrategy = PresentationPatternStrategy.MVI,
            diStrategy = DiStrategy.Hilt
        )

        beforeContainer {
            clearAllMocks()
        }

        afterSpec {
            tempDir.toFile().deleteRecursively()
            unmockkAll()
        }

        When("pattern is MVI") {
            every { modulePkgRepo.findModulePackage(moduleDir) } returns "com.example.feature"
            coEvery { intentRepo.generateIntent(any(), any(), any()) } returns FileGenerationResult(
                moduleDir.resolve("TestIntent.kt"),
                GenerationStatus.CREATED,
                "TestIntent.kt"
            )

            val result = step.execute(params)

            Then("it should generate Intent") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify {
                    intentRepo.generateIntent(
                        targetDir = any(),
                        packageName = "com.example.feature.test",
                        params = params
                    )
                }
            }
        }

        When("pattern is MVVM") {
            val mvvmParams = params.copy(patternStrategy = PresentationPatternStrategy.MVVM)
            val result = step.execute(mvvmParams)

            Then("it should skip execution") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 0) { intentRepo.generateIntent(any(), any(), any()) }
            }
        }
    }
})
