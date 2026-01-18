package com.jkjamies.cammp.feature.usecasegenerator.datasource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain

class DiModuleDataSourceImplTest : BehaviorSpec({

    val dataSource = DiModuleDataSourceImpl()

    Given("DiModuleDataSourceImpl") {
        When("generating Koin module content") {
            val result = dataSource.generateKoinModuleContent(
                existingContent = null,
                diPackage = "com.example.di",
                useCaseSimpleName = "MyUseCase",
                useCaseFqn = "com.example.usecase.MyUseCase",
                repositoryFqns = listOf("com.example.repo.MyRepo")
            )

            Then("it should contain single definition") {
                result shouldContain "single { MyUseCase(get()) }"
                result shouldContain "import com.example.usecase.MyUseCase"
            }
        }
        When("generating Koin module content with interface") {
            val result = dataSource.generateKoinModuleContent(
                existingContent = null,
                diPackage = "com.example.di",
                useCaseSimpleName = "MyUseCase",
                useCaseFqn = "com.example.domain.usecase.MyUseCase",
                repositoryFqns = listOf("com.example.domain.repository.MyRepo"),
                useCaseInterfaceFqn = "com.example.api.usecase.MyUseCase"
            )

            Then("it should contain interface binding") {
                result shouldContain "MyUseCase"
                result shouldContain "single"
                result shouldContain "get()"
            }
        }

        When("generating Hilt module content") {
            val result = dataSource.generateHiltModuleContent(
                existingContent = null,
                diPackage = "com.example.di",
                useCaseSimpleName = "MyUseCase",
                useCaseFqn = "com.example.domain.usecase.MyUseCase",
                useCaseInterfaceFqn = "com.example.api.usecase.MyUseCase"
            )

            Then("it should contain @Binds method") {
                result shouldContain "@Binds"
                result shouldContain "fun bindsMyUseCase"
                result shouldContain "MyUseCase"
            }
        }
    }
})
