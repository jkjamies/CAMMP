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
import io.kotest.matchers.string.shouldContain
import java.nio.file.Paths

/**
 * Tests for [IntentSpecFactoryImpl].
 */
class IntentSpecFactoryImplTest : BehaviorSpec({

    Given("IntentSpecFactoryImpl") {
        val factory = IntentSpecFactoryImpl()

        When("creating Intent for Home screen") {
            val params = PresentationParams(
                moduleDir = Paths.get("."),
                screenName = "Home",
                patternStrategy = PresentationPatternStrategy.MVI,
                diStrategy = DiStrategy.Hilt,
            )
            val spec = factory.create("com.example.presentation.home", params)
            val content = spec.toString()

            Then("it should generate internal sealed interface") {
                content shouldContain "internal sealed interface HomeIntent"
            }

            Then("it should contain NoOp object") {
                content shouldContain "object NoOp"
                content shouldContain "HomeIntent"
            }

            Then("it should use the correct package") {
                content shouldContain "package com.example.presentation.home"
            }
        }

        When("creating Intent for Settings screen") {
            val params = PresentationParams(
                moduleDir = Paths.get("."),
                screenName = "Settings",
                patternStrategy = PresentationPatternStrategy.MVI,
                diStrategy = DiStrategy.Metro,
            )
            val spec = factory.create("com.example.settings", params)
            val content = spec.toString()

            Then("class name should reflect screen name") {
                content shouldContain "SettingsIntent"
            }
        }
    }
})
