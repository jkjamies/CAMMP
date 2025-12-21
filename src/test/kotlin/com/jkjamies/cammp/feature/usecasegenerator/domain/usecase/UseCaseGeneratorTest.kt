package com.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseMergeOutcome
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Path

class UseCaseGeneratorTest : BehaviorSpec({

    Given("a use case generator") {
        val mockModulePkgRepo = mockk<ModulePackageRepository>()
        val mockDiRepo = mockk<UseCaseDiModuleRepository>()
        val mockGenRepo = mockk<UseCaseGenerationRepository>()

        val useCase = UseCaseGenerator(
            modulePkgRepo = mockModulePkgRepo,
            diRepo = mockDiRepo,
            generationRepo = mockGenRepo
        )

        When("invoked with valid params") {
            val domainDir = Path.of("project/app/src/main/kotlin/com/test/domain")
            
            val params = UseCaseParams(
                domainDir = domainDir,
                className = "GetSomething",
                useKoin = false,
                koinAnnotations = false,
                repositories = listOf("TestRepository")
            )

            // Mock behavior
            every { mockModulePkgRepo.findModulePackage(any()) } answers {
                val dir = firstArg<Path>()
                when {
                    dir.endsWith("domain") -> "com.test.domain"
                    dir.endsWith("di") -> "com.test.di"
                    else -> null
                }
            }

            every {
                mockDiRepo.mergeUseCaseModule(any(), any(), any(), any(), any(), any())
            } returns UseCaseMergeOutcome(Path.of("di/Module.kt"), "merged")

            every {
                mockGenRepo.generateUseCase(any(), any())
            } returns Path.of("domain/UseCase.kt")

            val result = useCase(params)

            Then("it should return success path") {
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Path.of("domain/UseCase.kt")
            }
        }
    }
})
