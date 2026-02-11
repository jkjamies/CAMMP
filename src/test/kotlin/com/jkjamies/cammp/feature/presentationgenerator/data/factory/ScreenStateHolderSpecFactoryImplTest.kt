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
 * Tests for [ScreenStateHolderSpecFactoryImpl].
 */
class ScreenStateHolderSpecFactoryImplTest : BehaviorSpec({

    Given("ScreenStateHolderSpecFactoryImpl") {
        val factory = ScreenStateHolderSpecFactoryImpl()

        When("creating state holder for Home screen") {
            val spec = factory.create("com.example.presentation.home", "Home")
            val content = spec.toString()

            Then("it should generate internal class") {
                content shouldContain "internal class HomeStateHolder"
            }

            Then("it should generate Composable remember function") {
                content shouldContain "Composable"
                content shouldContain "fun rememberHomeState"
                content shouldContain "HomeStateHolder"
            }

            Then("it should use remember pattern") {
                content shouldContain "remember"
            }

            Then("it should use the correct package") {
                content shouldContain "package com.example.presentation.home"
            }
        }

        When("creating state holder for Profile screen") {
            val spec = factory.create("com.example.presentation.profile", "Profile")
            val content = spec.toString()

            Then("names should reflect screen name") {
                content shouldContain "ProfileStateHolder"
                content shouldContain "rememberProfileState"
            }
        }
    }
})
