package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.data.factory.RepositorySpecFactory
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

class RepositoryGenerationRepositoryImplTest : BehaviorSpec({

    val specFactory = mockk<RepositorySpecFactory>()
    val repository = RepositoryGenerationRepositoryImpl(specFactory)

    Given("a RepositoryGenerationRepository") {
        val tempDir = Files.createTempDirectory("repo_gen_test")
        val className = "TestRepository"

        When("generating domain layer") {
            val packageName = "com.example.domain.repository"
            val params = mockParams(tempDir, className)

            val dummySpec = FileSpec.builder(packageName, className)
                .addType(TypeSpec.interfaceBuilder(className).build())
                .addFileComment("This has `data` in it")
                .build()

            every { specFactory.createDomainInterface(packageName, params) } returns dummySpec

            val resultPath = repository.generateDomainLayer(params, packageName, tempDir)

            Then("it should write file to correct path") {
                resultPath.exists() shouldBe true
            }

            Then("it should sanitize content replacing backticks") {
                val content = resultPath.readText()
                content shouldContain "This has data in it"
            }

            Then("it should contain generated class") {
                val content = resultPath.readText()
                content shouldContain "interface TestRepository"
            }
        }

        When("generating data layer") {
            val dataPackage = "com.example.data.repository"
            val domainPackage = "com.example.domain.repository"
            val params = mockParams(tempDir, className)

            val dummySpec = FileSpec.builder(dataPackage, "${className}Impl")
                .addType(TypeSpec.classBuilder("${className}Impl").build())
                .build()

            every { specFactory.createDataImplementation(dataPackage, domainPackage, params) } returns dummySpec

            val resultPath = repository.generateDataLayer(params, dataPackage, domainPackage)

            Then("it should write file") {
                resultPath.exists() shouldBe true
            }

            Then("it should end with correct filename") {
                resultPath.toString() shouldEndWith "TestRepositoryImpl.kt"
            }
        }
    }
})

private fun mockParams(tempDir: Path, className: String) = RepositoryParams(
    dataDir = tempDir.resolve("data"),
    className = className,
    includeDatasource = false,
    datasourceCombined = false,
    datasourceRemote = false,
    datasourceLocal = false,
    diStrategy = DiStrategy.Hilt
)
