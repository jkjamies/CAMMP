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

package com.jkjamies.cammp.feature.cleanarchitecture.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for [TemplateRepositoryImpl].
 */
class TemplateRepositoryImplTest : BehaviorSpec({

    Given("TemplateRepositoryImpl") {
        val repo = TemplateRepositoryImpl()

        When("requesting an existing template") {
            Then("it should return the resource text") {
                // This resource is part of the plugin resources and should always exist.
                val content = repo.getTemplateText("templates/cleanArchitecture/module/domain.gradle.kts")
                content shouldContain "plugins"
            }
        }

        When("requesting a missing template") {
            Then("it should throw") {
                val err = runCatching { repo.getTemplateText("templates/does-not-exist.txt") }.exceptionOrNull()
                err.shouldBeInstanceOf<IllegalStateException>()
                err.message shouldBe "Template not found: templates/does-not-exist.txt"
            }
        }
    }
})
