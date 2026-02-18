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

package com.jkjamies.cammp.feature.presentationgenerator.datasource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for the package inference rules used by [PackageMetadataDataSourceImpl].
 *
 * Reference: `src/main/kotlin/com/jkjamies/cammp/feature/presentationgenerator/datasource/PackageMetadataDataSourceImpl.kt`
 */
class PackageMetadataDataSourceImplTest : BehaviorSpec({

    Given("inferPresentationPackageFrom") {

        When("package list is empty") {
            Then("it returns null") {
                inferPresentationPackageFrom(emptySet()) shouldBe null
            }
        }

        When("an exact .presentation package exists") {
            Then("it returns it") {
                inferPresentationPackageFrom(
                    setOf(
                        "com.example.feature.presentation",
                        "com.example.feature.other",
                    )
                ) shouldBe "com.example.feature.presentation"
            }
        }

        When("an api package exists") {
            Then("it maps to base + .presentation") {
                inferPresentationPackageFrom(setOf("com.example.feature.api.usecase")) shouldBe
                    "com.example.feature.presentation"
            }
        }

        When("both api and domain packages exist") {
            Then("it prefers base from api") {
                inferPresentationPackageFrom(
                    setOf(
                        "com.example.other.domain",
                        "com.example.feature.api"
                    )
                ) shouldBe "com.example.feature.presentation"
            }
        }

        When("a domain package exists") {
            Then("it maps to base + .presentation") {
                inferPresentationPackageFrom(setOf("com.example.feature.domain.usecase")) shouldBe
                    "com.example.feature.presentation"
            }
        }

        When("a data package exists") {
            Then("it maps to base + .presentation") {
                inferPresentationPackageFrom(setOf("com.example.feature.data.repo")) shouldBe
                    "com.example.feature.presentation"
            }
        }

        When("no known package exists") {
            Then("it appends .presentation to the shortest") {
                inferPresentationPackageFrom(
                    setOf(
                        "com.example.long.package.name",
                        "com.ex",
                    )
                ) shouldBe "com.ex.presentation"
            }
        }
    }
})
