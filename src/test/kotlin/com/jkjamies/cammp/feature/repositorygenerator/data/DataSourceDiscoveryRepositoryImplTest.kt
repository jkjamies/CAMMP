package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.data.datasource.PackageMetadataDataSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * Tests for [DataSourceDiscoveryRepositoryImpl].
 */
class DataSourceDiscoveryRepositoryImplTest : BehaviorSpec({

    Given("DataSourceDiscoveryRepositoryImpl") {

        When("src/main/kotlin does not exist") {
            Then("it returns an empty map") {
                val tempDir = Files.createTempDirectory("ds_discovery_no_kotlin")
                try {
                    val packageMetadataDataSource = mockk<PackageMetadataDataSource>()
                    val repository = DataSourceDiscoveryRepositoryImpl(packageMetadataDataSource)

                    every { packageMetadataDataSource.findModulePackage(any()) } returns "com.example"

                    val result = repository.loadDataSourcesByType(tempDir.toString())
                    result.shouldBeEmpty()
                } finally {
                    tempDir.toFile().deleteRecursively()
                }
            }
        }

        When("package folders are missing") {
            Then("it returns an empty map (filters empty groups)") {
                val tempDir = Files.createTempDirectory("ds_discovery_missing_pkgs")
                try {
                    // create kotlin root but no datasource packages
                    tempDir.resolve("src/main/kotlin").createDirectories()

                    val packageMetadataDataSource = mockk<PackageMetadataDataSource>()
                    val repository = DataSourceDiscoveryRepositoryImpl(packageMetadataDataSource)
                    every { packageMetadataDataSource.findModulePackage(any()) } returns "com.example"

                    val result = repository.loadDataSourcesByType(tempDir.toString())
                    result.shouldBeEmpty()
                } finally {
                    tempDir.toFile().deleteRecursively()
                }
            }
        }

        When("datasource files exist in multiple groups") {
            Then("it returns a filtered map with sorted, distinct FQNs ending with DataSource") {
                val tempDir = Files.createTempDirectory("ds_discovery_groups")
                try {
                    val base = tempDir.resolve("src/main/kotlin/com/example")
                    base.createDirectories()

                    val packageMetadataDataSource = mockk<PackageMetadataDataSource>()
                    val repository = DataSourceDiscoveryRepositoryImpl(packageMetadataDataSource)
                    every { packageMetadataDataSource.findModulePackage(any()) } returns "com.example"

                    val combined = base.resolve("dataSource").also { it.createDirectories() }
                    val remote = base.resolve("remoteDataSource").also { it.createDirectories() }
                    val local = base.resolve("localDataSource").also { it.createDirectories() }

                    // combined: include duplicates and non-matching
                    combined.resolve("BDataSource.kt").writeText("interface BDataSource")
                    combined.resolve("ADataSource.kt").writeText("interface ADataSource")
                    combined.resolve("SomeOtherFile.kt").writeText("class SomeOtherFile")
                    combined.resolve("ADataSource.kt").writeText("interface ADataSource") // overwrite still fine

                    // remote
                    remote.resolve("ZRemoteDataSource.kt").writeText("interface ZRemoteDataSource")

                    // local
                    local.resolve("ALocalDataSource.kt").writeText("interface ALocalDataSource")

                    val result = repository.loadDataSourcesByType(tempDir.toString())

                    result shouldContainKey "Combined"
                    result["Combined"]!!.shouldContainExactly(
                        "com.example.dataSource.ADataSource",
                        "com.example.dataSource.BDataSource",
                    )

                    result shouldContainKey "Remote"
                    result["Remote"] shouldBe listOf("com.example.remoteDataSource.ZRemoteDataSource")

                    result shouldContainKey "Local"
                    result["Local"] shouldBe listOf("com.example.localDataSource.ALocalDataSource")

                    // ensure non-existent groups are filtered (none here)
                    result.shouldNotContainKey("Missing")
                } finally {
                    tempDir.toFile().deleteRecursively()
                }
            }
        }

        When("PackageMetadataDataSource throws") {
            Then("it returns an empty map") {
                val tempDir = Files.createTempDirectory("ds_discovery_throw")
                try {
                    tempDir.resolve("src/main/kotlin").createDirectories()

                    val packageMetadataDataSource = mockk<PackageMetadataDataSource>()
                    val repository = DataSourceDiscoveryRepositoryImpl(packageMetadataDataSource)

                    every { packageMetadataDataSource.findModulePackage(any()) } throws IllegalStateException("boom")

                    val result = repository.loadDataSourcesByType(tempDir.toString())
                    result.shouldBeEmpty()
                } finally {
                    tempDir.toFile().deleteRecursively()
                }
            }
        }
    }
})