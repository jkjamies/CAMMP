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
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Paths

/**
 * Tests for [UseCaseSpecFactoryImpl].
 */
class UseCaseSpecFactoryTest : BehaviorSpec({

    val factory = UseCaseSpecFactoryImpl()

    fun params(
        className: String = "MyUseCase",
        di: DiStrategy = DiStrategy.Hilt,
        repos: List<String> = listOf("MyRepository"),
    ) = UseCaseParams(
        domainDir = Paths.get("."),
        className = className,
        diStrategy = di,
        repositories = repos,
    )

    Given("UseCaseSpecFactoryImpl") {

        When("creating a spec for Hilt with repositories") {
            Then("it should include @Inject constructor with repo deps") {
                val spec = factory.create("com.example.usecase", params(di = DiStrategy.Hilt), "com.example.domain")
                val content = spec.toString()

                content shouldContain "package com.example.usecase"
                content shouldContain "import javax.inject.Inject"
                content shouldContain "class MyUseCase @Inject constructor("
                content shouldContain "import com.example.domain.repository.MyRepository"
                content shouldContain "private val myRepository: MyRepository"
                content shouldContain "suspend operator fun invoke()"
            }
        }

        When("creating a spec for Hilt with no repositories") {
            Then("it should still generate an @Inject constructor") {
                val spec = factory.create("com.example.usecase", params(di = DiStrategy.Hilt, repos = emptyList()), "com.example.domain")
                val content = spec.toString()

                content shouldContain "import javax.inject.Inject"
                content shouldContain "class MyUseCase @Inject constructor()"
            }
        }

        When("creating a spec for Metro without interface") {
            Then("it should include Metro @Inject on class") {
                val spec = factory.create(
                    "com.example.usecase",
                    params(di = DiStrategy.Metro),
                    "com.example.domain"
                )
                val content = spec.toString()

                content shouldContain "import dev.zacsweers.metro.Inject"
                content shouldContain "@Inject"
                content shouldNotContain "javax.inject"
                content shouldNotContain "ContributesBinding"
            }
        }

        When("creating a spec for Metro with interface") {
            Then("it should include @ContributesBinding and no explicit @Inject") {
                val spec = factory.create(
                    "com.example.usecase",
                    params(di = DiStrategy.Metro),
                    "com.example.domain",
                    interfaceFqn = "com.example.api.usecase.MyUseCase"
                )
                val content = spec.toString()

                content shouldContain "import dev.zacsweers.metro.ContributesBinding"
                content shouldContain "@ContributesBinding"
                content shouldContain "AppScope"
                content shouldNotContain "import dev.zacsweers.metro.Inject"
                content shouldNotContain "javax.inject"
            }
        }

        When("creating a spec for Koin with annotations") {
            Then("it should include @Single and a constructor") {
                val spec = factory.create(
                    "com.example.usecase",
                    params(di = DiStrategy.Koin(useAnnotations = true)),
                    "com.example.domain"
                )
                val content = spec.toString()

                content shouldContain "import org.koin.core.`annotation`.Single"
                content shouldContain "@Single"
                content shouldContain "class MyUseCase("
            }
        }

        When("creating a spec for Koin without annotations") {
            Then("it should not include @Single") {
                val spec = factory.create(
                    "com.example.usecase",
                    params(di = DiStrategy.Koin(useAnnotations = false)),
                    "com.example.domain"
                )
                val content = spec.toString()

                content shouldNotContain "@Single"
                content shouldNotContain "import org.koin.core.`annotation`.Single"

                // But repositories still require a constructor.
                content shouldContain "class MyUseCase("
            }
        }

        When("creating a spec for Koin without annotations and no repositories") {
            Then("it should generate a class with no primary constructor") {
                val spec = factory.create(
                    "com.example.usecase",
                    params(di = DiStrategy.Koin(useAnnotations = false), repos = emptyList()),
                    "com.example.domain"
                )
                val content = spec.toString()

                // KotlinPoet will render either `class MyUseCase` or `class MyUseCase { ... }`.
                // The key contract is: no primary constructor parentheses.
                content shouldNotContain "class MyUseCase("
            }
        }

        When("creating a spec with multiple repositories") {
            Then("it should generate params and private properties for each") {
                val spec = factory.create(
                    "com.example.usecase",
                    params(repos = listOf("FirstRepository", "SecondRepository")),
                    "com.example.domain"
                )
                val content = spec.toString()

                content shouldContain "import com.example.domain.repository.FirstRepository"
                content shouldContain "import com.example.domain.repository.SecondRepository"
                content shouldContain "private val firstRepository: FirstRepository"
                content shouldContain "private val secondRepository: SecondRepository"
            }
        }

        When("repository type starts with uppercase") {
            Then("it should lowercase only the first character for the constructor parameter") {
                val spec = factory.create(
                    "com.example.usecase",
                    params(repos = listOf("UserRepository")),
                    "com.example.domain"
                )
                val content = spec.toString()

                content shouldContain "private val userRepository: UserRepository"
            }
        }
    }
})
