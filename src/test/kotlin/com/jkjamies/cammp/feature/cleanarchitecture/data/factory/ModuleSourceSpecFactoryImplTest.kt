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

package com.jkjamies.cammp.feature.cleanarchitecture.data.factory

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.domain.model.DatasourceStrategy
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Path

/**
 * Tests for [ModuleSourceSpecFactoryImpl].
 */
class ModuleSourceSpecFactoryImplTest : BehaviorSpec({

    Given("ModuleSourceSpecFactoryImpl") {
        val factory = ModuleSourceSpecFactoryImpl()

        val params = CleanArchitectureParams(
            projectBasePath = Path.of("/project"),
            root = "feature",
            feature = "profile",
            orgCenter = "com.example",
            includePresentation = true,
            includeDiModule = true,
            datasourceStrategy = DatasourceStrategy.None,
        )

        When("creating a module package name") {
            val pkg = factory.packageName(params, moduleName = "domain", featureName = "profile")

            Then("it should be deterministic") {
                pkg shouldBe "com.example.feature.profile.domain"
            }
        }

        When("creating placeholder kotlin source") {
            val src = factory.placeholderKotlinFile(params, moduleName = "domain", featureName = "profile")

            Then("it should include package and placeholder type") {
                src.shouldContain("package com.example.feature.profile.domain")
                src.shouldContain("class Placeholder")
            }
        }
    }
})
