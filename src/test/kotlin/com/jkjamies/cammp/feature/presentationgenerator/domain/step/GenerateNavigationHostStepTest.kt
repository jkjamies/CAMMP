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
 * Test for [GenerateNavigationHostStep].
 */
class GenerateNavigationHostStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val navigationRepo = mockk<NavigationRepository>()
    val step = GenerateNavigationHostStep(modulePkgRepo, navigationRepo)

    Given("GenerateNavigationHostStep") {
        val tempDir = Files.createTempDirectory("nav_host_step_test")
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
                coVerify(exactly = 0) { navigationRepo.generateNavigationHost(any(), any(), any()) }
            }
        }

        When("includeNavigation is true") {
            val navParams = params.copy(includeNavigation = true)
            every { modulePkgRepo.findModulePackage(moduleDir) } returns "com.example.feature"
            coEvery { navigationRepo.generateNavigationHost(any(), any(), any()) } returns FileGenerationResult(
                moduleDir.resolve("NavHost.kt"),
                GenerationStatus.CREATED,
                "NavHost.kt"
            )

            val result = step.execute(navParams)

            Then("it should generate navigation host") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify {
                    navigationRepo.generateNavigationHost(
                        targetDir = any(),
                        packageName = "com.example.feature.navigation",
                        navHostName = "FeatureNavigationHost"
                    )
                }
            }
        }
    }
})
