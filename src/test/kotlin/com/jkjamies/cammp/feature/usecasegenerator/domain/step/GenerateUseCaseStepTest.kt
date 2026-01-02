package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Paths

class GenerateUseCaseStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val generationRepo = mockk<UseCaseGenerationRepository>()
    val step = GenerateUseCaseStep(generationRepo, modulePkgRepo)

    Given("a GenerateUseCaseStep") {
        val domainDir = Paths.get("/project/feature/domain")
        val params = UseCaseParams(
            domainDir = domainDir,
            className = "MyUseCase",
            diStrategy = DiStrategy.Hilt,
            repositories = emptyList()
        )

        When("execute is called and package is a standard domain module") {
            every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.feature.domain"
            coEvery { generationRepo.generateUseCase(any(), any(), any()) } returns Paths.get("/out/MyUseCase.kt")

            val result = step.execute(params)

            Then("it should generate use case in .usecase subpackage") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify {
                    generationRepo.generateUseCase(
                        params,
                        "com.example.feature.domain.usecase",
                        "com.example.feature.domain"
                    )
                }
            }
        }

        When("execute is called and package is already a usecase module") {
            every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.feature.domain.usecase"
            coEvery { generationRepo.generateUseCase(any(), any(), any()) } returns Paths.get("/out/MyUseCase.kt")

            val result = step.execute(params)

            Then("it should use the existing package") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify {
                    generationRepo.generateUseCase(
                        params,
                        "com.example.feature.domain.usecase",
                        "com.example.feature.domain"
                    )
                }
            }
        }

        When("repository throws an exception") {
            val expectedError = RuntimeException("Disk error")
            every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.feature.domain"
            coEvery { generationRepo.generateUseCase(any(), any(), any()) } throws expectedError

            val result = step.execute(params)

            Then("it should return Failure") {
                result.shouldBeInstanceOf<StepResult.Failure>()
                (result as StepResult.Failure).error shouldBe expectedError
            }
        }
    }
})
