package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseMergeOutcome
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Files

class UpdateDiStepTest : BehaviorSpec({

    val diRepo = mockk<UseCaseDiModuleRepository>()
    val modulePkgRepo = mockk<ModulePackageRepository>()
    val step = UpdateDiStep(diRepo, modulePkgRepo)

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("UpdateDiStep with real file structure") {
        val tempRoot = Files.createTempDirectory("cammp_test_update_di")
        val featureRoot = tempRoot.resolve("feature")
        val domainDir = featureRoot.resolve("domain")
        val diDir = featureRoot.resolve("di")

        // Create physical directories so .exists() checks pass
        Files.createDirectories(domainDir)
        Files.createDirectories(diDir)

        val params = UseCaseParams(
            domainDir = domainDir,
            className = "MyUseCase",
            diStrategy = DiStrategy.Koin(useAnnotations = false),
            repositories = listOf("MyRepo")
        )

        When("execute is called with valid paths") {
            every { modulePkgRepo.findModulePackage(diDir) } returns "com.example.di"
            every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.domain"
            
            coEvery { 
                diRepo.mergeUseCaseModule(any(), any(), any(), any(), any(), any()) 
            } returns UseCaseMergeOutcome(diDir.resolve("Module.kt"), "Updated")

            val result = step.execute(params)

            Then("it should call mergeUseCaseModule") {
                result.shouldBeInstanceOf<StepResult.Success>()
                
                coVerify { 
                    diRepo.mergeUseCaseModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        useCaseSimpleName = "MyUseCase",
                        useCaseFqn = "com.example.domain.usecase.MyUseCase",
                        repositoryFqns = listOf("com.example.domain.repository.MyRepo"),
                        diStrategy = params.diStrategy
                    )
                }
            }
        }
        
        // Cleanup
        tempRoot.toFile().deleteRecursively()
    }
})
