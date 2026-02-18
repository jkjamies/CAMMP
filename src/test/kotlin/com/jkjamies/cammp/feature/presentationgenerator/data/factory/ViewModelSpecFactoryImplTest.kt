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

package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Paths

/**
 * Tests for [ViewModelSpecFactoryImpl].
 */
class ViewModelSpecFactoryImplTest : BehaviorSpec({

    fun testParams(
        screenName: String = "Home",
        pattern: PresentationPatternStrategy = PresentationPatternStrategy.MVI,
        diStrategy: DiStrategy = DiStrategy.Hilt,
        selectedUseCases: List<String> = emptyList(),
    ) = PresentationParams(
        moduleDir = Paths.get("."),
        screenName = screenName,
        patternStrategy = pattern,
        diStrategy = diStrategy,
        selectedUseCases = selectedUseCases,
    )

    Given("ViewModelSpecFactoryImpl") {
        val factory = ViewModelSpecFactoryImpl()
        val pkg = "com.example.presentation.home"

        When("MVI pattern with Hilt DI") {
            val spec = factory.create(pkg, testParams(diStrategy = DiStrategy.Hilt))
            val content = spec.toString()

            Then("it should extend ViewModel") {
                content shouldContain "ViewModel()"
            }

            Then("it should have HiltViewModel annotation and Inject constructor") {
                content shouldContain "HiltViewModel"
                content shouldContain "Inject"
            }

            Then("it should have MutableStateFlow and StateFlow properties") {
                content shouldContain "_state"
                content shouldContain "MutableStateFlow"
                content shouldContain "val state"
                content shouldContain "asStateFlow"
            }

            Then("it should have handleIntent function with when block") {
                content shouldContain "fun handleIntent"
                content shouldContain "when (intent)"
                content shouldContain "NoOp"
            }

            Then("it should be internal") {
                content shouldContain "internal class HomeViewModel"
            }
        }

        When("MVI pattern with Metro DI") {
            val spec = factory.create(pkg, testParams(diStrategy = DiStrategy.Metro))
            val content = spec.toString()

            Then("it should have ViewModelKey and ContributesIntoMap annotations") {
                content shouldContain "ViewModelKey"
                content shouldContain "ContributesIntoMap"
                content shouldContain "AppScope"
            }

            Then("it should not have Hilt or javax.inject annotations") {
                content shouldNotContain "HiltViewModel"
                content shouldNotContain "javax.inject"
            }

            Then("it should not have explicit @Inject (implied by @ContributesIntoMap)") {
                // ContributesIntoMap implies @Inject
                content shouldNotContain "import dev.zacsweers.metro.Inject"
            }
        }

        When("MVI pattern with Koin DI (annotations enabled)") {
            val spec = factory.create(pkg, testParams(diStrategy = DiStrategy.Koin(useAnnotations = true)))
            val content = spec.toString()

            Then("it should have KoinViewModel annotation") {
                content shouldContain "KoinViewModel"
            }

            Then("it should not have HiltViewModel annotation") {
                content shouldNotContain "HiltViewModel"
            }
        }

        When("MVI pattern with Koin DI (no annotations)") {
            val spec = factory.create(pkg, testParams(diStrategy = DiStrategy.Koin(useAnnotations = false)))
            val content = spec.toString()

            Then("it should not have any DI annotations") {
                content shouldNotContain "HiltViewModel"
                content shouldNotContain "KoinViewModel"
                content shouldNotContain "Inject"
            }
        }

        When("MVVM pattern") {
            val spec = factory.create(pkg, testParams(pattern = PresentationPatternStrategy.MVVM))
            val content = spec.toString()

            Then("it should not generate handleIntent function") {
                content shouldNotContain "handleIntent"
            }

            Then("it should still have state properties") {
                content shouldContain "_state"
                content shouldContain "val state"
            }
        }

        When("selected use cases are provided") {
            val useCases = listOf("com.example.domain.GetItemsUseCase", "com.example.domain.SaveItemUseCase")
            val spec = factory.create(pkg, testParams(selectedUseCases = useCases))
            val content = spec.toString()

            Then("it should add use cases as constructor parameters") {
                content shouldContain "getItemsUseCase"
                content shouldContain "GetItemsUseCase"
                content shouldContain "saveItemUseCase"
                content shouldContain "SaveItemUseCase"
            }
        }

        When("creating for a different screen name") {
            val spec = factory.create("com.example.presentation.profile", testParams(screenName = "Profile"))
            val content = spec.toString()

            Then("names should reflect the screen") {
                content shouldContain "ProfileViewModel"
                content shouldContain "ProfileUiState"
                content shouldContain "ProfileIntent"
            }
        }
    }
})
