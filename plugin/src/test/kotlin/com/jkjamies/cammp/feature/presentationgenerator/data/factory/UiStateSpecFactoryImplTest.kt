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

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain

/**
 * Tests for [UiStateSpecFactoryImpl].
 */
class UiStateSpecFactoryImplTest : BehaviorSpec({

    Given("UiStateSpecFactoryImpl") {
        val factory = UiStateSpecFactoryImpl()

        When("creating UiState for Home screen") {
            val spec = factory.create("com.example.presentation.home", "Home")
            val content = spec.toString()

            Then("it should generate internal data class with isLoading") {
                content shouldContain "internal data class HomeUiState"
                content shouldContain "isLoading"
                content shouldContain "Boolean"
                content shouldContain "false"
            }

            Then("it should use the correct package") {
                content shouldContain "package com.example.presentation.home"
            }
        }

        When("creating UiState for Profile screen") {
            val spec = factory.create("com.example.presentation.profile", "Profile")
            val content = spec.toString()

            Then("class name should reflect screen name") {
                content shouldContain "ProfileUiState"
                content shouldContain "package com.example.presentation.profile"
            }
        }
    }
})
