package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.data.factory.DataSourceSpecFactory
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Tests for [DatasourceScaffoldRepositoryImpl].
 */
class DatasourceScaffoldRepositoryImplTest : BehaviorSpec({

    Given("DatasourceScaffoldRepositoryImpl") {

        When("generateInterface is called") {
            Then("it creates directories, calls the spec factory, and writes <className>.kt") {
                val tempDir = Files.createTempDirectory("scaffold_interface_test")
                try {
                    val specFactory = mockk<DataSourceSpecFactory>()
                    val repository = DatasourceScaffoldRepositoryImpl(specFactory)

                    val packageName = "com.example"
                    val className = "UserDataSource"

                    val dummySpec = FileSpec.builder(packageName, className)
                        .addType(TypeSpec.interfaceBuilder(className).build())
                        .build()

                    every { specFactory.createInterface(packageName, className) } returns dummySpec

                    val result = repository.generateInterface(tempDir, packageName, className)

                    result.exists() shouldBe true
                    result.toString() shouldBe tempDir.resolve("$className.kt").toString()
                    result.readText() shouldBe dummySpec.toString()

                    verify(exactly = 1) { specFactory.createInterface(packageName, className) }
                } finally {
                    tempDir.toFile().deleteRecursively()
                }
            }
        }

        When("generateImplementation is called") {
            Then("it creates directories, calls the spec factory, and writes <className>.kt") {
                val tempDir = Files.createTempDirectory("scaffold_impl_test")
                try {
                    val specFactory = mockk<DataSourceSpecFactory>()
                    val repository = DatasourceScaffoldRepositoryImpl(specFactory)

                    val packageName = "com.example"
                    val className = "UserDataSourceImpl"
                    val interfacePackage = "com.example"
                    val interfaceName = "UserDataSource"
                    val useKoin = true

                    val dummySpec = FileSpec.builder(packageName, className)
                        .addType(TypeSpec.classBuilder(className).build())
                        .build()

                    every {
                        specFactory.createImplementation(
                            packageName = packageName,
                            className = className,
                            interfacePackage = interfacePackage,
                            interfaceName = interfaceName,
                            useKoin = useKoin,
                        )
                    } returns dummySpec

                    val result = repository.generateImplementation(
                        directory = tempDir,
                        packageName = packageName,
                        className = className,
                        interfacePackage = interfacePackage,
                        interfaceName = interfaceName,
                        useKoin = useKoin,
                    )

                    result.exists() shouldBe true
                    result.toString() shouldBe tempDir.resolve("$className.kt").toString()
                    result.readText() shouldBe dummySpec.toString()

                    verify(exactly = 1) {
                        specFactory.createImplementation(
                            packageName = packageName,
                            className = className,
                            interfacePackage = interfacePackage,
                            interfaceName = interfaceName,
                            useKoin = useKoin,
                        )
                    }
                } finally {
                    tempDir.toFile().deleteRecursively()
                }
            }
        }
    }
})