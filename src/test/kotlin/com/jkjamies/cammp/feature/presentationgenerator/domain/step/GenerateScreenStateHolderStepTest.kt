package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenStateHolderRepository
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
 * Test for [GenerateScreenStateHolderStep].
 */
class GenerateScreenStateHolderStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val screenStateHolderRepo = mockk<ScreenStateHolderRepository>()
    val step = GenerateScreenStateHolderStep(modulePkgRepo, screenStateHolderRepo)

    Given("GenerateScreenStateHolderStep") {
        val tempDir = Files.createTempDirectory("screen_state_step_test")
        val moduleDir = tempDir.resolve("feature")
        Files.createDirectories(moduleDir)

        val params = PresentationParams(
            moduleDir = moduleDir,
            screenName = "Test",
            patternStrategy = PresentationPatternStrategy.MVVM,
            diStrategy = DiStrategy.Hilt,
            useScreenStateHolder = false
        )

        beforeContainer {
            clearAllMocks()
        }

        afterSpec {
            tempDir.toFile().deleteRecursively()
            unmockkAll()
        }

        When("useScreenStateHolder is false") {
            val result = step.execute(params)

            Then("it should skip execution") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 0) { screenStateHolderRepo.generateScreenStateHolder(any(), any(), any()) }
            }
        }

        When("useScreenStateHolder is true") {
            val holderParams = params.copy(useScreenStateHolder = true)
            every { modulePkgRepo.findModulePackage(moduleDir) } returns "com.example.feature"
            coEvery { screenStateHolderRepo.generateScreenStateHolder(any(), any(), any()) } returns FileGenerationResult(
                moduleDir.resolve("TestStateHolder.kt"),
                GenerationStatus.CREATED,
                "TestStateHolder.kt"
            )

            val result = step.execute(holderParams)

            Then("it should generate screen state holder") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify {
                    screenStateHolderRepo.generateScreenStateHolder(
                        targetDir = any(),
                        packageName = "com.example.feature.test",
                        params = holderParams
                    )
                }
            }
        }
    }
})
