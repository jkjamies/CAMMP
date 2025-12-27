package com.jkjamies.cammp.feature.usecasegenerator.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files

/**
 * Test class for [UseCaseDiModuleRepositoryImpl].
 */
class UseCaseDiModuleRepositoryImplTest : BehaviorSpec({

    Given("a UseCaseDiModuleRepositoryImpl") {
        val repository = UseCaseDiModuleRepositoryImpl()
        val tempDir = Files.createTempDirectory("usecase_di_test")
        val diDir = tempDir.resolve("di")
        val diPackage = "com.example.di"

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("mergeUseCaseModule is called with useKoin=false") {
            val result = repository.mergeUseCaseModule(
                diDir = diDir,
                diPackage = diPackage,
                useCaseSimpleName = "GetUsers",
                useCaseFqn = "com.example.domain.usecase.GetUsers",
                repositoryFqns = emptyList(),
                useKoin = false
            )

            Then("it should return skipped status") {
                result.status shouldBe "skipped"
            }
        }

        When("mergeUseCaseModule is called with useKoin=true (new file)") {
            val result = repository.mergeUseCaseModule(
                diDir = diDir,
                diPackage = diPackage,
                useCaseSimpleName = "GetUsers",
                useCaseFqn = "com.example.domain.usecase.GetUsers",
                repositoryFqns = listOf("com.example.domain.repository.UserRepository"),
                useKoin = true
            )

            Then("it should create the Koin module") {
                result.status shouldBe "created"
                Files.exists(result.outPath)
                val content = Files.readString(result.outPath)
                content shouldContain "module {"
                content shouldContain "single { GetUsers(get()) }"
            }
        }

        When("mergeUseCaseModule is called with existing file") {
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/UseCaseModule.kt")
            Files.createDirectories(existingFile.parent)
            val existingContent = """
                package com.example.di
                
                import org.koin.dsl.module
                import org.koin.core.module.Module
                import com.example.domain.usecase.OldUseCase
                
                val useCaseModule = module {
                    single { OldUseCase() }
                }
            """.trimIndent()
            Files.writeString(existingFile, existingContent)

            val result = repository.mergeUseCaseModule(
                diDir = diDir,
                diPackage = diPackage,
                useCaseSimpleName = "NewUseCase",
                useCaseFqn = "com.example.domain.usecase.NewUseCase",
                repositoryFqns = emptyList(),
                useKoin = true
            )

            Then("it should append to existing module") {
                result.status shouldBe "updated"
                val content = Files.readString(result.outPath)
                content shouldContain "single { OldUseCase() }"
                content shouldContain "single { NewUseCase() }"
            }
        }
    }
})
