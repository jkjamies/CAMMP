package com.jkjamies.cammp.feature.repositorygenerator.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import java.nio.file.Files

/**
 * Test class for [DataSourceDiscoveryRepositoryImpl].
 */
class DataSourceDiscoveryRepositoryImplTest : BehaviorSpec({

    beforeSpec {
        mockkConstructor(ModulePackageRepositoryImpl::class)
    }

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a DataSourceDiscoveryRepositoryImpl") {
        val repository = DataSourceDiscoveryRepositoryImpl()

        When("loadDataSourcesByType is called with an invalid path") {
            val result = repository.loadDataSourcesByType("/invalid/path")

            Then("it should return an empty map") {
                result.shouldBeEmpty()
            }
        }

        When("loadDataSourcesByType is called with a valid path containing data sources") {
            val tempDir = Files.createTempDirectory("test_datasource_discovery")
            val srcMainKotlin = tempDir.resolve("src/main/kotlin")
            val basePkg = "com.example.data"
            val packagePath = srcMainKotlin.resolve("com/example/data")
            
            Files.createDirectories(packagePath.resolve("dataSource"))
            Files.createDirectories(packagePath.resolve("remoteDataSource"))
            Files.createDirectories(packagePath.resolve("localDataSource"))

            Files.createFile(packagePath.resolve("dataSource/UserDataSource.kt"))
            Files.createFile(packagePath.resolve("remoteDataSource/UserRemoteDataSource.kt"))
            Files.createFile(packagePath.resolve("localDataSource/UserLocalDataSource.kt"))
            Files.createFile(packagePath.resolve("dataSource/OtherFile.kt"))

            every { anyConstructed<ModulePackageRepositoryImpl>().findModulePackage(any()) } returns basePkg

            val result = repository.loadDataSourcesByType(tempDir.toString())

            Then("it should return the map of data sources") {
                result shouldContainKey "Combined"
                result["Combined"] shouldBe listOf("com.example.data.dataSource.UserDataSource")
                
                result shouldContainKey "Remote"
                result["Remote"] shouldBe listOf("com.example.data.remoteDataSource.UserRemoteDataSource")
                
                result shouldContainKey "Local"
                result["Local"] shouldBe listOf("com.example.data.localDataSource.UserLocalDataSource")
            }
            
            tempDir.toFile().deleteRecursively()
        }
        
        When("loadDataSourcesByType encounters an exception during processing") {
             every { anyConstructed<ModulePackageRepositoryImpl>().findModulePackage(any()) } throws RuntimeException("Something went wrong")
             
             val tempDir = Files.createTempDirectory("test_datasource_exception")
             Files.createDirectories(tempDir.resolve("src/main/kotlin"))
             
             val result = repository.loadDataSourcesByType(tempDir.toString())

             Then("it should return an empty map") {
                 result.shouldBeEmpty()
             }
             
             tempDir.toFile().deleteRecursively()
        }
    }
})
