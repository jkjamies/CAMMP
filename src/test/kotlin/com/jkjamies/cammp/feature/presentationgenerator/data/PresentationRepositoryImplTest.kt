package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.data.datasource.PackageMetadataDataSource
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FlowStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.IntentRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationDiModuleRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationMergeOutcome
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.UiStateRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ViewModelRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Files
import kotlin.io.path.createDirectories

/**
 * Test class for [PresentationRepositoryImpl].
 */
class PresentationRepositoryImplTest : BehaviorSpec({

    val fs = mockk<FileSystemRepository>(relaxed = true)
    val packageMetadataDataSource = mockk<PackageMetadataDataSource>(relaxed = true)
    val diRepo = mockk<PresentationDiModuleRepository>(relaxed = true)
    val uiStateRepo = mockk<UiStateRepository>(relaxed = true)
    val screenStateHolderRepo = mockk<ScreenStateHolderRepository>(relaxed = true)
    val flowStateHolderRepo = mockk<FlowStateHolderRepository>(relaxed = true)
    val intentRepo = mockk<IntentRepository>(relaxed = true)
    val navigationRepo = mockk<NavigationRepository>(relaxed = true)
    val screenRepo = mockk<ScreenRepository>(relaxed = true)
    val viewModelRepo = mockk<ViewModelRepository>(relaxed = true)

    val presentationRepository = PresentationRepositoryImpl(
        fs,
        packageMetadataDataSource,
        diRepo,
        uiStateRepo,
        screenStateHolderRepo,
        flowStateHolderRepo,
        intentRepo,
        navigationRepo,
        screenRepo,
        viewModelRepo
    )

    val tempDir = Files.createTempDirectory("pres_repo_test")
    val moduleDir = tempDir.resolve("testModule")

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        tempDir.toFile().deleteRecursively()
        unmockkAll()
    }

    Given("a presentation repository") {

        When("generating with no optional features") {
            val params = PresentationParams(
                moduleDir = moduleDir,
                screenName = "TestScreen",
                patternMVI = false,
                patternMVVM = true,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false
            )
            every { packageMetadataDataSource.findModulePackage(any()) } returns "com.example.test"
            coEvery { screenRepo.generateScreen(any(), any(), any(), any(), any()) } returns FileGenerationResult(
                mockk(),
                GenerationStatus.CREATED,
                "TestScreen.kt"
            )
            coEvery { viewModelRepo.generateViewModel(any(), any(), any(), any(), any(), any(), any(), any()) } returns FileGenerationResult(
                mockk(),
                GenerationStatus.CREATED,
                "TestScreenViewModel.kt"
            )
            coEvery { uiStateRepo.generateUiState(any(), any(), any()) } returns FileGenerationResult(
                mockk(),
                GenerationStatus.CREATED,
                "TestScreenUiState.kt"
            )

            presentationRepository.generate(params)

            Then("it should generate screen, viewmodel and uistate") {
                coVerify { screenRepo.generateScreen(any(), "com.example.test.testScreen", "TestScreen", false, false) }
                coVerify { viewModelRepo.generateViewModel(any(), "com.example.test.testScreen", "TestScreen", false, false, false, false, emptyList()) }
                coVerify { uiStateRepo.generateUiState(any(), "com.example.test.testScreen", "TestScreen") }
            }
        }

        When("generating with MVI") {
            val params = PresentationParams(
                moduleDir = moduleDir,
                screenName = "TestScreen",
                patternMVI = true,
                patternMVVM = false,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false
            )
            every { packageMetadataDataSource.findModulePackage(any()) } returns "com.example.test"
            coEvery { intentRepo.generateIntent(any(), any(), any()) } returns FileGenerationResult(
                mockk(),
                GenerationStatus.CREATED,
                "TestScreenIntent.kt"
            )

            presentationRepository.generate(params)

            Then("it should generate intent") {
                coVerify { intentRepo.generateIntent(any(), "com.example.test.testScreen", "TestScreen") }
            }
        }

        When("generating with ScreenStateHolder") {
            val params = PresentationParams(
                moduleDir = moduleDir,
                screenName = "TestScreen",
                patternMVI = false,
                patternMVVM = true,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false,
                useScreenStateHolder = true
            )
            every { packageMetadataDataSource.findModulePackage(any()) } returns "com.example.test"
            coEvery { screenStateHolderRepo.generateScreenStateHolder(any(), any(), any()) } returns FileGenerationResult(
                mockk(),
                GenerationStatus.CREATED,
                "TestScreenStateHolder.kt"
            )

            presentationRepository.generate(params)

            Then("it should generate screen state holder") {
                coVerify { screenStateHolderRepo.generateScreenStateHolder(any(), "com.example.test.testScreen", "TestScreen") }
            }
        }

        When("generating with FlowStateHolder") {
            val params = PresentationParams(
                moduleDir = moduleDir,
                screenName = "TestScreen",
                patternMVI = false,
                patternMVVM = true,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false,
                useFlowStateHolder = true
            )
            every { packageMetadataDataSource.findModulePackage(any()) } returns "com.example.test.presentation"
            coEvery { flowStateHolderRepo.generateFlowStateHolder(any(), any(), any()) } returns FileGenerationResult(
                mockk(),
                GenerationStatus.CREATED,
                "TestFlowStateHolder.kt"
            )

            presentationRepository.generate(params)

            Then("it should generate flow state holder") {
                coVerify { flowStateHolderRepo.generateFlowStateHolder(any(), "com.example.test.presentation", "TestFlowStateHolder") }
            }
        }

        When("generating with Navigation") {
            val params = PresentationParams(
                moduleDir = moduleDir,
                screenName = "TestScreen",
                patternMVI = false,
                patternMVVM = true,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false,
                includeNavigation = true
            )
            every { packageMetadataDataSource.findModulePackage(any()) } returns "com.example.test.presentation"
            coEvery { navigationRepo.generateNavigationHost(any(), any(), any()) } returns FileGenerationResult(
                mockk(),
                GenerationStatus.CREATED,
                "TestNavigationHost.kt"
            )
            coEvery { navigationRepo.generateDestination(any(), any(), any(), any()) } returns FileGenerationResult(
                mockk(),
                GenerationStatus.CREATED,
                "TestScreenDestination.kt"
            )

            presentationRepository.generate(params)

            Then("it should generate navigation host and destination") {
                coVerify { navigationRepo.generateNavigationHost(any(), "com.example.test.presentation.navigation", "TestNavigationHost") }
                coVerify { navigationRepo.generateDestination(any(), "com.example.test.presentation", "TestScreen", "testScreen") }
            }
        }

        When("generating with Navigation and existing directories") {
            // Pre-create directories
            val pkgDir = moduleDir.resolve("src/main/kotlin/com/example/test/presentation")
            val navDir = pkgDir.resolve("navigation")
            val destDir = navDir.resolve("destinations")
            destDir.createDirectories()

            val params = PresentationParams(
                moduleDir = moduleDir,
                screenName = "TestScreen",
                patternMVI = false,
                patternMVVM = true,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false,
                includeNavigation = true
            )
            every { packageMetadataDataSource.findModulePackage(any()) } returns "com.example.test.presentation"
            coEvery { navigationRepo.generateNavigationHost(any(), any(), any()) } returns FileGenerationResult(
                mockk(),
                GenerationStatus.CREATED,
                "TestNavigationHost.kt"
            )
            coEvery { navigationRepo.generateDestination(any(), any(), any(), any()) } returns FileGenerationResult(
                mockk(),
                GenerationStatus.CREATED,
                "TestScreenDestination.kt"
            )

            presentationRepository.generate(params)

            Then("it should use existing directories and generate files") {
                coVerify { navigationRepo.generateNavigationHost(any(), "com.example.test.presentation.navigation", "TestNavigationHost") }
                coVerify { navigationRepo.generateDestination(any(), "com.example.test.presentation", "TestScreen", "testScreen") }
            }
        }

        When("generating with Koin") {
            val diDir = tempDir.resolve("di")
            Files.createDirectories(diDir) // Create the directory so exists() returns true

            val params = PresentationParams(
                moduleDir = moduleDir,
                screenName = "TestScreen",
                patternMVI = false,
                patternMVVM = true,
                diHilt = false,
                diKoin = true,
                diKoinAnnotations = false
            )
            every { packageMetadataDataSource.findModulePackage(moduleDir) } returns "com.example.test.presentation"
            every { packageMetadataDataSource.findModulePackage(diDir) } returns "com.example.test.di"
            coEvery { diRepo.mergeViewModelModule(any(), any(), any(), any(), any()) } returns PresentationMergeOutcome(
                mockk(),
                "created"
            )
            every { fs.exists(any()) } returns true

            presentationRepository.generate(params)

            Then("it should merge viewmodel module") {
                // The logic in PresentationRepositoryImpl now infers DI package from presentation package
                // "com.example.test.presentation" -> "com.example.test.di"
                coVerify { diRepo.mergeViewModelModule(any(), "com.example.test.di", "TestScreenViewModel", "com.example.test.presentation.testScreen.TestScreenViewModel", 0) }
            }
        }
    }
})
