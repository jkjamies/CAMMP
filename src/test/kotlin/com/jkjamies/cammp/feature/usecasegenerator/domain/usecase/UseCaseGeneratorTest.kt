package com.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseMergeOutcome
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import java.nio.file.Files
import java.nio.file.Path

/**
 * Test class for [UseCaseGenerator].
 */
class UseCaseGeneratorTest : BehaviorSpec({

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a use case generator") {
        val mockModulePkgRepo = mockk<ModulePackageRepository>()
        val mockDiRepo = mockk<UseCaseDiModuleRepository>()
        val mockGenRepo = mockk<UseCaseGenerationRepository>()

        val useCase = UseCaseGenerator(
            modulePkgRepo = mockModulePkgRepo,
            diRepo = mockDiRepo,
            generationRepo = mockGenRepo
        )

        When("invoked with valid params and no DI needed (Koin annotations)") {
            val domainDir = Path.of("project/app/src/main/kotlin/com/test/domain")
            
            val params = UseCaseParams(
                domainDir = domainDir,
                className = "GetSomething",
                useKoin = true,
                koinAnnotations = true,
                repositories = listOf("TestRepository")
            )

            every { mockModulePkgRepo.findModulePackage(domainDir) } returns "com.test.domain"
            every { mockGenRepo.generateUseCase(any(), any()) } returns Path.of("domain/UseCase.kt")

            val result = useCase(params)

            Then("it should return success path and not merge DI module") {
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Path.of("domain/UseCase.kt")
                verify(exactly = 0) { mockDiRepo.mergeUseCaseModule(any(), any(), any(), any(), any(), any()) }
            }
        }

        When("invoked with valid params and DI needed but DI dir missing") {
            val tempDir = Files.createTempDirectory("test_domain")
            val domainDir = tempDir.resolve("src/main/kotlin/com/test/domain")
            Files.createDirectories(domainDir)
            
            val params = UseCaseParams(
                domainDir = domainDir,
                className = "GetSomething",
                useKoin = false,
                koinAnnotations = false,
                repositories = listOf("TestRepository")
            )

            every { mockModulePkgRepo.findModulePackage(domainDir) } returns "com.test.domain"
            every { mockGenRepo.generateUseCase(any(), any()) } returns Path.of("domain/UseCase.kt")

            val result = useCase(params)

            Then("it should return success path and not merge DI module") {
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Path.of("domain/UseCase.kt")
                verify(exactly = 0) { mockDiRepo.mergeUseCaseModule(any(), any(), any(), any(), any(), any()) }
            }
            
            tempDir.toFile().deleteRecursively()
        }

        When("invoked with valid params and DI needed and DI dir exists") {
            val tempDir = Files.createTempDirectory("test_project")
            val domainDir = tempDir.resolve("src/main/kotlin/com/test/domain")
            val diDir = tempDir.resolve("src/main/kotlin/com/test/di")
            Files.createDirectories(domainDir)
            Files.createDirectories(diDir)
            
            val params = UseCaseParams(
                domainDir = domainDir,
                className = "GetSomething",
                useKoin = false,
                koinAnnotations = false,
                repositories = listOf("TestRepository")
            )

            every { mockModulePkgRepo.findModulePackage(domainDir) } returns "com.test.domain"
            every { mockModulePkgRepo.findModulePackage(match { it.endsWith("di") }) } returns "com.test.di"
            
            every { mockGenRepo.generateUseCase(any(), any()) } returns Path.of("domain/UseCase.kt")
            every { mockDiRepo.mergeUseCaseModule(any(), any(), any(), any(), any(), any()) } returns UseCaseMergeOutcome(Path.of("di/Module.kt"), "merged")

            val result = useCase(params)

            Then("it should return success path and merge DI module") {
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe Path.of("domain/UseCase.kt")
                
                verify { 
                    mockDiRepo.mergeUseCaseModule(
                        diDir = match { it.endsWith("di") },
                        diPackage = "com.test.di",
                        useCaseSimpleName = "GetSomethingUseCase",
                        useCaseFqn = "com.test.domain.usecase.GetSomethingUseCase",
                        repositoryFqns = listOf("com.test.domain.repository.TestRepository"),
                        useKoin = false
                    ) 
                }
            }
            
            tempDir.toFile().deleteRecursively()
        }

        When("invoked and package inference fails") {
            val domainDir = Path.of("invalid/path")
            val params = UseCaseParams(
                domainDir = domainDir,
                className = "GetSomething",
                useKoin = false,
                koinAnnotations = false
            )

            every { mockModulePkgRepo.findModulePackage(domainDir) } returns null

            val result = useCase(params)

            Then("it should return failure") {
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Could not determine existing package for selected domain module"
            }
        }
        
        When("invoked and DI package inference fails") {
             val tempDir = Files.createTempDirectory("test_project_fail_di")
            val domainDir = tempDir.resolve("src/main/kotlin/com/test/domain")
            val diDir = tempDir.resolve("src/main/kotlin/com/test/di")
            Files.createDirectories(domainDir)
            Files.createDirectories(diDir)
            
            val params = UseCaseParams(
                domainDir = domainDir,
                className = "GetSomething",
                useKoin = false,
                koinAnnotations = false,
                repositories = listOf("TestRepository")
            )

            every { mockModulePkgRepo.findModulePackage(domainDir) } returns "com.test.domain"
            every { mockModulePkgRepo.findModulePackage(match { it.endsWith("di") }) } returns null
            
            every { mockGenRepo.generateUseCase(any(), any()) } returns Path.of("domain/UseCase.kt")

            val result = useCase(params)

            Then("it should return success but not merge DI because DI package not found") {
                result.isSuccess shouldBe true
                verify(exactly = 0) { mockDiRepo.mergeUseCaseModule(any(), any(), any(), any(), any(), any()) }
            }
            
            tempDir.toFile().deleteRecursively()
        }
    }
})
