package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ViewModelRepository
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
 * Test for [GenerateViewModelStep].
 */
class GenerateViewModelStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val viewModelRepo = mockk<ViewModelRepository>()
    val step = GenerateViewModelStep(modulePkgRepo, viewModelRepo)

    Given("GenerateViewModelStep") {
        val tempDir = Files.createTempDirectory("viewmodel_step_test")
        val moduleDir = tempDir.resolve("feature")
        Files.createDirectories(moduleDir)

        val params = PresentationParams(
            moduleDir = moduleDir,
            screenName = "Test",
            patternStrategy = PresentationPatternStrategy.MVVM,
            diStrategy = DiStrategy.Hilt
        )

        beforeContainer {
            clearAllMocks()
        }

        afterSpec {
            tempDir.toFile().deleteRecursively()
            unmockkAll()
        }

        When("execute is called") {
            every { modulePkgRepo.findModulePackage(moduleDir) } returns "com.example.feature"
            coEvery { viewModelRepo.generateViewModel(any(), any(), any()) } returns FileGenerationResult(
                moduleDir.resolve("TestViewModel.kt"),
                GenerationStatus.CREATED,
                "TestViewModel.kt"
            )

            val result = step.execute(params)

            Then("it should generate ViewModel") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify {
                    viewModelRepo.generateViewModel(
                        targetDir = any(),
                        packageName = "com.example.feature.test",
                        params = params
                    )
                }
            }
        }
    }
})
