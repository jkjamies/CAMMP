package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.data.factory.UseCaseSpecFactoryImpl
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.readText

class UseCaseGenerationRepositoryImplTest : BehaviorSpec({

    val specFactory = UseCaseSpecFactoryImpl()
    val repository = UseCaseGenerationRepositoryImpl(specFactory)

    Given("UseCaseGenerationRepositoryImpl") {
        val tempDir = Files.createTempDirectory("usecase_gen_repo_test")
        val domainDir = tempDir.resolve("domain")
        Files.createDirectories(domainDir)

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("generating use case") {
            val params = UseCaseParams(
                domainDir = domainDir,
                className = "MyUseCase",
                diStrategy = DiStrategy.Hilt,
                repositories = emptyList()
            )
            
            // Fixed: generateUseCase now takes baseDomainPackage as 3rd argument
            val result = repository.generateUseCase(
                params, 
                "com.example.domain.usecase",
                "com.example.domain"
            )

            Then("it should write file to correct path") {
                result.exists() shouldBe true
                result.toString() shouldContain "MyUseCase.kt"
                
                val content = result.readText()
                content shouldContain "package com.example.domain.usecase"
                content shouldContain "class MyUseCase"
            }
        }
    }
})
