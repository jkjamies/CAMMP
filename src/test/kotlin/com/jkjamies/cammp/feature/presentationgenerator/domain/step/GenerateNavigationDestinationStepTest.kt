package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
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
 * Test for [GenerateNavigationDestinationStep].
 */
class GenerateNavigationDestinationStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val navigationRepo = mockk<NavigationRepository>()
    val step = GenerateNavigationDestinationStep(modulePkgRepo, navigationRepo)

    Given("GenerateNavigationDestinationStep") {
        val tempDir = Files.createTempDirectory("nav_dest_step_test")
        val moduleDir = tempDir.resolve("feature")
        Files.createDirectories(moduleDir)

        val params = PresentationParams(
            moduleDir = moduleDir,
            screenName = "Test",
            patternStrategy = PresentationPatternStrategy.MVVM,
            diStrategy = DiStrategy.Hilt,
            includeNavigation = false
        )

        beforeContainer {
            clearAllMocks()
        }

        afterSpec {
            tempDir.toFile().deleteRecursively()
            unmockkAll()
        }

        When("includeNavigation is false") {
            val result = step.execute(params)

            Then("it should skip execution") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 0) { navigationRepo.generateDestination(any(), any(), any(), any()) }
            }
        }

        When("includeNavigation is true") {
            val navParams = params.copy(includeNavigation = true)
            every { modulePkgRepo.findModulePackage(moduleDir) } returns "com.example.feature"
            coEvery { navigationRepo.generateDestination(any(), any(), any(), any()) } returns FileGenerationResult(
                moduleDir.resolve("TestDestination.kt"),
                GenerationStatus.CREATED,
                "TestDestination.kt"
            )

            val result = step.execute(navParams)

            Then("it should generate navigation destination") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify {
                    navigationRepo.generateDestination(
                        targetDir = any(),
                        packageName = "com.example.feature",
                        params = navParams,
                        screenFolder = "test"
                    )
                }
            }
        }
    }
})
