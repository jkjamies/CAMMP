package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.DiModuleDataSource
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Tests for [UseCaseDiModuleRepositoryImpl].
 */
class UseCaseDiModuleRepositoryImplTest : BehaviorSpec({

    fun newRepo(dataSource: DiModuleDataSource) = UseCaseDiModuleRepositoryImpl(dataSource)

    fun tempDir() = Files.createTempDirectory("usecase_di_test").also { it.toFile().deleteOnExit() }

    Given("UseCaseDiModuleRepositoryImpl") {

        When("merging Hilt module") {
            Then("it should return skipped and a predictable output path") {
                val ds = mockk<DiModuleDataSource>()
                val repo = newRepo(ds)
                val root = tempDir()

                val result = repo.mergeUseCaseModule(
                    diDir = root,
                    diPackage = "com.example.di",
                    useCaseSimpleName = "UC",
                    useCaseFqn = "com.example.domain.usecase.UC",
                    repositoryFqns = emptyList(),
                    diStrategy = DiStrategy.Hilt,
                )

                result.status shouldBe "skipped"
                result.outPath.toString().replace('\\', '/') shouldContain "src/main/kotlin/com/example/di/UseCaseModule.kt"

                // No actual file write is expected for Hilt branch.
                result.outPath.exists() shouldBe false

                root.toFile().deleteRecursively()
            }
        }

        When("merging Koin module when file does not exist") {
            Then("it should create the file and report created") {
                val ds = mockk<DiModuleDataSource>()
                every { ds.generateKoinModuleContent(null, "com.example.di", any(), any(), any()) } returns "content"

                val repo = newRepo(ds)
                val root = tempDir()

                val result = repo.mergeUseCaseModule(
                    diDir = root,
                    diPackage = "com.example.di",
                    useCaseSimpleName = "UC",
                    useCaseFqn = "UC",
                    repositoryFqns = emptyList(),
                    diStrategy = DiStrategy.Koin(useAnnotations = false),
                )

                result.status shouldBe "created"
                result.outPath.exists() shouldBe true
                result.outPath.readText() shouldBe "content"

                root.toFile().deleteRecursively()
            }
        }

        When("merging Koin module when file exists and content differs") {
            Then("it should update the file and report updated") {
                val ds = mockk<DiModuleDataSource>()
                every { ds.generateKoinModuleContent("old content", "com.example.di", any(), any(), any()) } returns "new content"

                val repo = newRepo(ds)
                val root = tempDir()
                val diPath = root.resolve("src/main/kotlin/com/example/di").also { it.createDirectories() }
                val existingFile = diPath.resolve("UseCaseModule.kt")
                existingFile.writeText("old content")

                val result = repo.mergeUseCaseModule(
                    diDir = root,
                    diPackage = "com.example.di",
                    useCaseSimpleName = "UC",
                    useCaseFqn = "UC",
                    repositoryFqns = emptyList(),
                    diStrategy = DiStrategy.Koin(useAnnotations = false),
                )

                result.status shouldBe "updated"
                result.outPath.readText() shouldBe "new content"

                root.toFile().deleteRecursively()
            }
        }

        When("merging Koin module when file exists and content is the same") {
            Then("it should still write the file but report exists") {
                val ds = mockk<DiModuleDataSource>()
                every { ds.generateKoinModuleContent("same content", "com.example.di", any(), any(), any()) } returns "same content"

                val repo = newRepo(ds)
                val root = tempDir()
                val diPath = root.resolve("src/main/kotlin/com/example/di").also { it.createDirectories() }
                val existingFile = diPath.resolve("UseCaseModule.kt")
                existingFile.writeText("same content")

                val result = repo.mergeUseCaseModule(
                    diDir = root,
                    diPackage = "com.example.di",
                    useCaseSimpleName = "UC",
                    useCaseFqn = "UC",
                    repositoryFqns = emptyList(),
                    diStrategy = DiStrategy.Koin(useAnnotations = false),
                )

                result.status shouldBe "exists"
                result.outPath.readText() shouldBe "same content"

                root.toFile().deleteRecursively()
            }
        }
    }
})
