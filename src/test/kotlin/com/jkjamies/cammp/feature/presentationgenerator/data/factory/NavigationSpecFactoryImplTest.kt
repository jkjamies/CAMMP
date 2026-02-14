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
 * Tests for [NavigationSpecFactoryImpl].
 */
class NavigationSpecFactoryImplTest : BehaviorSpec({

    Given("NavigationSpecFactoryImpl") {
        val factory = NavigationSpecFactoryImpl()

        When("creating a navigation host") {
            val spec = factory.createHost("com.example.presentation.home", "HomeNavHost")
            val content = spec.toString()

            Then("it should be a Composable internal function") {
                content shouldContain "Composable"
                content shouldContain "internal fun HomeNavHost"
            }

            Then("it should set up NavHostController") {
                content shouldContain "NavHostController"
                content shouldContain "rememberNavController"
            }

            Then("it should reference NavHost") {
                content shouldContain "NavHost"
            }

            Then("it should include TODO comments") {
                content shouldContain "TODO"
            }
        }

        When("creating a navigation destination") {
            val spec = factory.createDestination("com.example.presentation.home", "Home", "home")
            val content = spec.toString()

            Then("it should generate a Serializable destination object") {
                content shouldContain "Serializable"
                content shouldContain "internal object HomeDestination"
            }

            Then("it should generate a NavGraphBuilder extension function") {
                content shouldContain "NavGraphBuilder"
                content shouldContain "fun NavGraphBuilder.home"
            }

            Then("it should use the destinations sub-package") {
                content shouldContain "package com.example.presentation.home.navigation.destinations"
            }

            Then("it should reference composable and the screen") {
                content shouldContain "composable"
                content shouldContain "HomeDestination"
            }
        }

        When("getting destination comments") {
            val comments = factory.getDestinationComments("Home", "home")

            Then("it should contain example code referencing the screen name") {
                comments shouldContain "HomeDestination"
                comments shouldContain "home"
                comments shouldContain "NavGraphBuilder"
                comments shouldContain "NavController"
            }
        }
    }
})
