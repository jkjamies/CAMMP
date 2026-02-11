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
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for [ScreenSpecFactoryImpl].
 */
class ScreenSpecFactoryImplTest : BehaviorSpec({

    Given("ScreenSpecFactoryImpl") {
        val factory = ScreenSpecFactoryImpl()

        When("DI strategy is Hilt") {
            Then("it should use hiltViewModel default and be internal") {
                val spec = factory.create(
                    packageName = "com.example.presentation.home",
                    screenName = "Home",
                    diStrategy = DiStrategy.Hilt,
                )

                val text = spec.toString()
                text.contains("internal fun Home") shouldBe true
                text.contains("hiltViewModel") shouldBe true
                text.contains("viewModel: HomeViewModel") shouldBe true
            }
        }

        When("DI strategy is Metro") {
            Then("it should use hiltViewModel default (same as Hilt)") {
                val spec = factory.create(
                    packageName = "com.example.presentation.home",
                    screenName = "Home",
                    diStrategy = DiStrategy.Metro,
                )

                val text = spec.toString()
                text.contains("internal fun Home") shouldBe true
                text.contains("hiltViewModel") shouldBe true
            }
        }

        When("DI strategy is Koin") {
            Then("it should use koinViewModel default") {
                val spec = factory.create(
                    packageName = "com.example.presentation.home",
                    screenName = "Home",
                    diStrategy = DiStrategy.Koin(useAnnotations = false),
                )

                val text = spec.toString()
                text.contains("internal fun Home") shouldBe true
                text.contains("koinViewModel") shouldBe true
                text.contains("viewModel: HomeViewModel") shouldBe true
            }
        }
    }
})

