package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.data.factory.DataSourceSpecFactory
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.readText

class DatasourceScaffoldRepositoryImplTest : BehaviorSpec({

    val specFactory = mockk<DataSourceSpecFactory>()
    val repository = DatasourceScaffoldRepositoryImpl(specFactory)

    Given("a DatasourceScaffoldRepositoryImpl") {
        val tempDir = Files.createTempDirectory("scaffold_test")

        When("generateInterface is called") {
            val packageName = "com.example"
            val className = "UserDataSource"
            
            val dummySpec = FileSpec.builder(packageName, className)
                .addType(TypeSpec.interfaceBuilder(className).build())
                .build()
            
            every { specFactory.createInterface(packageName, className) } returns dummySpec

            val result = repository.generateInterface(tempDir, packageName, className)

            Then("it writes the file") {
                result.exists() shouldBe true
                result.readText() shouldBe dummySpec.toString()
            }
        }

        When("generateImplementation is called") {
            val packageName = "com.example"
            val className = "UserDataSourceImpl"
            
            val dummySpec = FileSpec.builder(packageName, className)
                .addType(TypeSpec.classBuilder(className).build())
                .build()

            every { 
                specFactory.createImplementation(packageName, className, any(), any(), any()) 
            } returns dummySpec

            val result = repository.generateImplementation(
                tempDir, packageName, className, "com.example", "UserDataSource", false
            )

            Then("it writes the file") {
                result.exists() shouldBe true
                result.readText() shouldBe dummySpec.toString()
            }
        }
        
        afterSpec {
            tempDir.toFile().deleteRecursively()
        }
    }
})