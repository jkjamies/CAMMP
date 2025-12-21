package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files
import kotlin.io.path.readText

class UseCaseGenerationRepositoryImplTest : BehaviorSpec({

    Given("a use case generation repository") {
        val mockModulePkgRepo = mockk<ModulePackageRepository>()
        val repository = UseCaseGenerationRepositoryImpl(mockModulePkgRepo)
        val tempDir = Files.createTempDirectory("usecase_gen_test")
        val domainDir = tempDir.resolve("domain")

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        every { mockModulePkgRepo.findModulePackage(any()) } returns "com.example.domain"

        When("generating use case with Hilt") {
            val params = UseCaseParams(
                domainDir = domainDir,
                className = "GetItemsUseCase",
                useKoin = false,
                koinAnnotations = false,
                repositories = listOf("ItemRepository")
            )
            val packageName = "com.example.domain.usecase"

            val resultPath = repository.generateUseCase(params, packageName)

            Then("it should create the file") {
                Files.exists(resultPath)
            }
            Then("it should contain the class definition") {
                val content = resultPath.readText()
                content shouldContain "package com.example.domain.usecase"
                content shouldContain "class GetItemsUseCase"
                content shouldContain "@Inject"
                content shouldContain "suspend operator fun invoke"
            }
            Then("it should inject repositories") {
                val content = resultPath.readText()
                content shouldContain "itemRepository: ItemRepository"
            }
        }

        When("generating use case with Koin Annotations") {
            val params = UseCaseParams(
                domainDir = domainDir,
                className = "KoinUseCase",
                useKoin = true,
                koinAnnotations = true,
                repositories = emptyList()
            )
            val packageName = "com.example.domain.usecase"

            val resultPath = repository.generateUseCase(params, packageName)

            Then("it should contain the Single annotation") {
                val content = resultPath.readText()
                content shouldContain "@Single"
                content shouldContain "import org.koin.core.annotation.Single"
            }
        }
    }
})
