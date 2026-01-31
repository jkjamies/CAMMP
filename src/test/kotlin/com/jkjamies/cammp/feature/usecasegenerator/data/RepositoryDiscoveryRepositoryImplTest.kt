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

import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.PackageMetadataDataSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files

/**
 * Tests for [RepositoryDiscoveryRepositoryImpl].
 */
class RepositoryDiscoveryRepositoryImplTest : BehaviorSpec({

    fun newRepo(): Pair<PackageMetadataDataSource, RepositoryDiscoveryRepositoryImpl> {
        val ds = mockk<PackageMetadataDataSource>()
        return ds to RepositoryDiscoveryRepositoryImpl(ds)
    }

    Given("RepositoryDiscoveryRepositoryImpl") {

        When("loadRepositories is called with an invalid path") {
            Then("it should return an empty list") {
                val (_, repo) = newRepo()
                repo.loadRepositories("/invalid/path").shouldBeEmpty()
            }
        }

        When("loadRepositories is called with a valid path containing repositories") {
            Then("it should return the list of repository names") {
                val tempDir = Files.createTempDirectory("test_repo_discovery")
                val (ds, repo) = newRepo()

                val srcMainKotlin = tempDir.resolve("src/main/kotlin")
                val packagePath = srcMainKotlin.resolve("com/example/domain/repository")
                Files.createDirectories(packagePath)
                Files.createFile(packagePath.resolve("AuthRepository.kt"))
                Files.createFile(packagePath.resolve("UserRepository.kt"))

                every { ds.findModulePackage(any()) } returns "com.example.domain.usecase"

                repo.loadRepositories(tempDir.toString()) shouldBe listOf("AuthRepository", "UserRepository")

                tempDir.toFile().deleteRecursively()
            }
        }

        When("loadRepositories encounters an exception") {
            Then("it should return an empty list") {
                val tempFile = Files.createTempFile("test_file", ".txt")
                val (_, repo) = newRepo()

                repo.loadRepositories(tempFile.toString()).shouldBeEmpty()
                Files.deleteIfExists(tempFile)
            }
        }

        When("loadRepositories resolves repository package by stripping .usecase") {
            Then("it should map <base>.domain.usecase to <base>.domain.repository") {
                val tempDir = Files.createTempDirectory("test_repo_discovery_usecase")
                val (ds, repo) = newRepo()

                // Simulate a discovered usecase package
                every { ds.findModulePackage(any()) } returns "com.example.feature.domain.usecase"

                // Create the repository directory the implementation should look for:
                // com/example/feature/domain/repository
                val repoDir = tempDir
                    .resolve("src/main/kotlin")
                    .resolve("com/example/feature/domain/repository")
                Files.createDirectories(repoDir)
                Files.createFile(repoDir.resolve("ARepository.kt"))

                repo.loadRepositories(tempDir.toString()) shouldBe listOf("ARepository")

                tempDir.toFile().deleteRecursively()
            }
        }

        When("loadRepositories resolves repository package by NOT having .usecase") {
            Then("it should map <base>.domain to <base>.domain.repository") {
                val tempDir = Files.createTempDirectory("test_repo_discovery_domain")
                val (ds, repo) = newRepo()

                every { ds.findModulePackage(any()) } returns "com.example.feature.domain"

                val repoDir = tempDir
                    .resolve("src/main/kotlin")
                    .resolve("com/example/feature/domain/repository")
                Files.createDirectories(repoDir)
                Files.createFile(repoDir.resolve("BRepository.kt"))

                repo.loadRepositories(tempDir.toString()) shouldBe listOf("BRepository")

                tempDir.toFile().deleteRecursively()
            }
        }

        When("src/main/kotlin does not exist") {
            Then("it should return empty list") {
                val tempDir = Files.createTempDirectory("test_repo_discovery_no_src")
                val (ds, repo) = newRepo()
                every { ds.findModulePackage(any()) } returns "com.example.feature.domain.usecase"

                // No src/main/kotlin created
                repo.loadRepositories(tempDir.toString()).shouldBeEmpty()

                tempDir.toFile().deleteRecursively()
            }
        }

        When("repository package directory does not exist") {
            Then("it should return empty list") {
                val tempDir = Files.createTempDirectory("test_repo_discovery_no_repo_dir")
                val (ds, repo) = newRepo()
                every { ds.findModulePackage(any()) } returns "com.example.feature.domain.usecase"
                Files.createDirectories(tempDir.resolve("src/main/kotlin"))

                // repo package path is missing
                repo.loadRepositories(tempDir.toString()).shouldBeEmpty()

                tempDir.toFile().deleteRecursively()
            }
        }
    }
})
