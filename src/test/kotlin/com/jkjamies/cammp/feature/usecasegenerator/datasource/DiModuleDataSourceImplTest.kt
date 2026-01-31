/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
