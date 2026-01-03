package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.data.datasource.PackageMetadataDataSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class DataSourceDiscoveryRepositoryImplTest : BehaviorSpec({

    val packageMetadataDataSource = mockk<PackageMetadataDataSource>()
    val repository = DataSourceDiscoveryRepositoryImpl(packageMetadataDataSource)

    Given("a DataSourceDiscoveryRepositoryImpl") {
        val tempDir = Files.createTempDirectory("ds_discovery_test")
        val srcDir = tempDir.resolve("src/main/kotlin/com/example")
        srcDir.createDirectories()

        every { packageMetadataDataSource.findModulePackage(any()) } returns "com.example"

        When("loadDataSourcesByType is called") {
            // Setup files
            val dsDir = srcDir.resolve("dataSource")
            dsDir.createDirectories()
            dsDir.resolve("UserDataSource.kt").writeText("interface UserDataSource")

            val remoteDir = srcDir.resolve("remoteDataSource")
            remoteDir.createDirectories()
            remoteDir.resolve("UserRemoteDataSource.kt").writeText("interface UserRemoteDataSource")

            // Create a non-datasource file to ensure filtering works
            dsDir.resolve("SomeOtherFile.kt").writeText("class SomeOtherFile")

            val result = repository.loadDataSourcesByType(tempDir.toString())

            Then("it returns grouped data sources") {
                result shouldContainKey "Combined"
                result["Combined"] shouldBe listOf("com.example.dataSource.UserDataSource")
                
                result shouldContainKey "Remote"
                result["Remote"] shouldBe listOf("com.example.remoteDataSource.UserRemoteDataSource")
            }
        }
        
        afterSpec {
            tempDir.toFile().deleteRecursively()
        }
    }
})