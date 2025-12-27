package com.jkjamies.cammp.feature.usecasegenerator.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import java.nio.file.Files

/**
 * Test class for [RepositoryDiscoveryRepositoryImpl].
 */
class RepositoryDiscoveryRepositoryImplTest : BehaviorSpec({

    beforeSpec {
        mockkConstructor(ModulePackageRepositoryImpl::class)
    }

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a RepositoryDiscoveryRepositoryImpl") {
        val repository = RepositoryDiscoveryRepositoryImpl()

        When("loadRepositories is called with an invalid path") {
            val result = repository.loadRepositories("/invalid/path")

            Then("it should return an empty list") {
                result.shouldBeEmpty()
            }
        }

        When("loadRepositories is called with a valid path containing repositories") {
            val tempDir = Files.createTempDirectory("test_repo_discovery")
            
            val srcMainKotlin = tempDir.resolve("src/main/kotlin")
            val packagePath = srcMainKotlin.resolve("com/example/domain/repository")
            Files.createDirectories(packagePath)

            Files.createFile(packagePath.resolve("AuthRepository.kt"))
            Files.createFile(packagePath.resolve("UserRepository.kt"))

            every { anyConstructed<ModulePackageRepositoryImpl>().findModulePackage(any()) } returns "com.example.domain.usecase"

            val result = repository.loadRepositories(tempDir.toString())

            Then("it should return the list of repository names") {
                result shouldBe listOf("AuthRepository", "UserRepository")
            }
            
            tempDir.toFile().deleteRecursively()
        }

        When("loadRepositories encounters an exception") {
             val tempFile = Files.createTempFile("test_file", ".txt")
             val result = repository.loadRepositories(tempFile.toString())

             Then("it should return an empty list") {
                 result.shouldBeEmpty()
             }
             
             Files.deleteIfExists(tempFile)
        }
    }
})
