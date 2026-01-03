package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FlowStateHolderRepository
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

/**
 * Test for [GenerateFlowStateHolderStep].
 */
class GenerateFlowStateHolderStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val flowStateHolderRepo = mockk<FlowStateHolderRepository>()
    val step = GenerateFlowStateHolderStep(modulePkgRepo, flowStateHolderRepo)

    Given("GenerateFlowStateHolderStep") {
        val tempDir = Files.createTempDirectory("flow_step_test")
        val moduleDir = tempDir.resolve("feature")
        Files.createDirectories(moduleDir)

        val params = PresentationParams(
            moduleDir = moduleDir,
            screenName = "Test",
            patternStrategy = PresentationPatternStrategy.MVVM,
            diStrategy = DiStrategy.Hilt,
            useFlowStateHolder = false
        )

        beforeContainer {
            clearAllMocks()
        }

        afterSpec {
            tempDir.toFile().deleteRecursively()
            unmockkAll()
        }

        When("useFlowStateHolder is false") {
            val result = step.execute(params)

            Then("it should skip execution") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 0) { flowStateHolderRepo.generateFlowStateHolder(any(), any(), any(), any()) }
            }
        }

        When("useFlowStateHolder is true") {
            val flowParams = params.copy(useFlowStateHolder = true)
            every { modulePkgRepo.findModulePackage(moduleDir) } returns "com.example.feature"
            coEvery { flowStateHolderRepo.generateFlowStateHolder(any(), any(), any(), any()) } returns FileGenerationResult(
                moduleDir.resolve("FeatureFlowStateHolder.kt"),
                GenerationStatus.CREATED,
                "FeatureFlowStateHolder.kt"
            )

            val result = step.execute(flowParams)

            Then("it should generate flow state holder") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify {
                    flowStateHolderRepo.generateFlowStateHolder(
                        targetDir = any(),
                        packageName = "com.example.feature",
                        flowName = "FeatureFlowStateHolder",
                        params = flowParams
                    )
                }
            }
        }
    }
})
