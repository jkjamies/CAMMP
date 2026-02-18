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
 * Tests for [FlowStateHolderSpecFactoryImpl].
 */
class FlowStateHolderSpecFactoryImplTest : BehaviorSpec({

    Given("FlowStateHolderSpecFactoryImpl") {
        val factory = FlowStateHolderSpecFactoryImpl()

        When("creating flow state holder for ProfileFlowStateHolder") {
            val spec = factory.create("com.example.presentation.profile", "ProfileFlowStateHolder")
            val content = spec.toString()

            Then("it should generate internal class with navController and scope") {
                content shouldContain "internal class ProfileFlowStateHolder"
                content shouldContain "navController"
                content shouldContain "NavHostController"
                content shouldContain "scope"
                content shouldContain "CoroutineScope"
            }

            Then("it should derive the correct remember function name") {
                content shouldContain "rememberProfileFlowState"
            }

            Then("it should have Composable annotation") {
                content shouldContain "Composable"
            }

            Then("it should use default parameter values") {
                content shouldContain "rememberNavController"
                content shouldContain "rememberCoroutineScope"
            }

            Then("it should use the correct package") {
                content shouldContain "package com.example.presentation.profile"
            }
        }

        When("creating flow state holder for SettingsFlowStateHolder") {
            val spec = factory.create("com.example.presentation.settings", "SettingsFlowStateHolder")
            val content = spec.toString()

            Then("remember function name should match") {
                content shouldContain "rememberSettingsFlowState"
            }
        }
    }
})
