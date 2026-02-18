/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.DiModuleDataSource
import com.jkjamies.cammp.domain.model.DiStrategy
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
        When("merging Hilt module with interface") {
            Then("it should call data source to generate content") {
                val ds = mockk<DiModuleDataSource>()
                every { ds.generateHiltModuleContent(any(), any(), any(), any(), any()) } returns "hilt content"
                val repo = newRepo(ds)
                val root = tempDir()

                val result = repo.mergeUseCaseModule(
                    diDir = root,
                    diPackage = "com.example.di",
                    useCaseSimpleName = "UC",
                    useCaseFqn = "com.example.domain.usecase.UC",
                    repositoryFqns = emptyList(),
                    diStrategy = DiStrategy.Hilt,
                    useCaseInterfaceFqn = "com.example.api.usecase.UC"
                )

                result.status shouldBe "created"
                result.outPath.exists() shouldBe true
                result.outPath.readText() shouldBe "hilt content"

                root.toFile().deleteRecursively()
            }
        }

        When("merging Koin module with interface") {
            Then("it should pass interface to data source") {
                val ds = mockk<DiModuleDataSource>()
                every { ds.generateKoinModuleContent(any(), any(), any(), any(), any(), "com.example.api.usecase.UC") } returns "koin interface content"
                val repo = newRepo(ds)
                val root = tempDir()

                val result = repo.mergeUseCaseModule(
                    diDir = root,
                    diPackage = "com.example.di",
                    useCaseSimpleName = "UC",
                    useCaseFqn = "com.example.domain.usecase.UC",
                    repositoryFqns = emptyList(),
                    diStrategy = DiStrategy.Koin(useAnnotations = false),
                    useCaseInterfaceFqn = "com.example.api.usecase.UC"
                )

                result.status shouldBe "created"
                result.outPath.readText() shouldBe "koin interface content"

                root.toFile().deleteRecursively()
            }
        }
    }
})
