package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.DiModuleDataSource
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class UseCaseDiModuleRepositoryImplTest : BehaviorSpec({

    val dataSource = mockk<DiModuleDataSource>()
    val repository = UseCaseDiModuleRepositoryImpl(dataSource)

    Given("UseCaseDiModuleRepositoryImpl") {
        val tempDir = Files.createTempDirectory("usecase_di_test")
        val diPath = tempDir.resolve("src/main/kotlin/com/example/di")
        diPath.createDirectories()

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("merging Koin module (New File)") {
            every { dataSource.generateKoinModuleContent(any(), any(), any(), any(), any()) } returns "content"

            val result = repository.mergeUseCaseModule(
                diDir = tempDir,
                diPackage = "com.example.di",
                useCaseSimpleName = "UC",
                useCaseFqn = "UC",
                repositoryFqns = emptyList(),
                diStrategy = DiStrategy.Koin(false)
            )

            Then("it should call dataSource and create file") {
                verify { dataSource.generateKoinModuleContent(null, "com.example.di", any(), any(), any()) }
                result.status shouldBe "created"
            }
        }

        When("merging Koin module (Existing File, Content Changed)") {
            val existingFile = diPath.resolve("UseCaseModule.kt")
            existingFile.writeText("old content")
            
            every { dataSource.generateKoinModuleContent(any(), any(), any(), any(), any()) } returns "new content"

            val result = repository.mergeUseCaseModule(
                diDir = tempDir,
                diPackage = "com.example.di",
                useCaseSimpleName = "UC",
                useCaseFqn = "UC",
                repositoryFqns = emptyList(),
                diStrategy = DiStrategy.Koin(false)
            )

            Then("it should update file") {
                verify { dataSource.generateKoinModuleContent("old content", "com.example.di", any(), any(), any()) }
                result.status shouldBe "updated"
            }
        }

        When("merging Koin module (Existing File, Content Unchanged)") {
            val existingFile = diPath.resolve("UseCaseModule.kt")
            existingFile.writeText("same content")
            
            every { dataSource.generateKoinModuleContent(any(), any(), any(), any(), any()) } returns "same content"

            val result = repository.mergeUseCaseModule(
                diDir = tempDir,
                diPackage = "com.example.di",
                useCaseSimpleName = "UC",
                useCaseFqn = "UC",
                repositoryFqns = emptyList(),
                diStrategy = DiStrategy.Koin(false)
            )

            Then("it should report exists") {
                result.status shouldBe "exists"
            }
        }

        When("merging Hilt module") {
            val result = repository.mergeUseCaseModule(
                diDir = tempDir,
                diPackage = "com.example.di",
                useCaseSimpleName = "UC",
                useCaseFqn = "UC",
                repositoryFqns = emptyList(),
                diStrategy = DiStrategy.Hilt
            )

            Then("it should skip") {
                result.status shouldBe "skipped"
            }
        }
    }
})
