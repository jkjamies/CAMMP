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

package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.data.factory.UseCaseSpecFactoryImpl
import com.jkjamies.cammp.domain.model.DiStrategy
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
            
            val result = repository.generateUseCase(
                params, 
                "com.example.domain.usecase",
                "com.example.domain"
            )

            Then("it should write file to correct path") {
                result.useCasePath.exists() shouldBe true
                result.useCasePath.toString() shouldContain "MyUseCase.kt"
                
                val content = result.useCasePath.readText()
                content shouldContain "package com.example.domain.usecase"
                content shouldContain "class MyUseCase"
            }
        }
        When("generating use case with API module present") {
            val apiDir = tempDir.resolve("api")
            Files.createDirectories(apiDir)

            val params = UseCaseParams(
                domainDir = domainDir,
                className = "MyUseCase",
                diStrategy = DiStrategy.Hilt,
                repositories = emptyList()
            )

            val result = repository.generateUseCase(
                params,
                "com.example.domain.usecase",
                "com.example.domain",
                apiDir
            )

            Then("it should generate interface in api module") {
                val interfaceFile = apiDir.resolve("src/main/kotlin/com/example/api/usecase/MyUseCase.kt")
                interfaceFile.exists() shouldBe true
                interfaceFile.readText() shouldContain "interface MyUseCase"
            }

            Then("it should generate implementation implementing the interface") {
                result.useCasePath.exists() shouldBe true
                val content = result.useCasePath.readText()
                content shouldContain "class MyUseCase"
                content shouldContain "MyUseCase"
            }
        }
    }
})
