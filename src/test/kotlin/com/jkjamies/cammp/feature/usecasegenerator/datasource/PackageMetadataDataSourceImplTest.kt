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

package com.jkjamies.cammp.feature.usecasegenerator.datasource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for [PackageMetadataDataSourceImpl].
 *
 */
class PackageMetadataDataSourceImplTest : BehaviorSpec({

    Given("inferUseCasePackageFrom") {

        When("package list is empty") {
            Then("it returns null") {
                inferUseCasePackageFrom(emptySet()) shouldBe null
            }
        }

        When("an exact .domain.usecase package exists") {
            Then("it returns it") {
                inferUseCasePackageFrom(
                    setOf(
                        "com.example.feature.domain.usecase",
                        "com.example.feature.other",
                    )
                ) shouldBe "com.example.feature.domain.usecase"
            }
        }

        When("an exact .domain package exists") {
            Then("it appends .usecase") {
                inferUseCasePackageFrom(setOf("com.example.feature.domain")) shouldBe
                    "com.example.feature.domain.usecase"
            }
        }

        When("a package contains .domain") {
            Then("it truncates to .domain and appends .usecase") {
                inferUseCasePackageFrom(setOf("com.example.feature.domain.something")) shouldBe
                    "com.example.feature.domain.usecase"
            }
        }

        When("no domain-related package exists") {
            Then("it appends .usecase to the shortest") {
                inferUseCasePackageFrom(
                    setOf(
                        "com.example.feature.long.package",
                        "com.ex",
                    )
                ) shouldBe "com.ex.usecase"
            }
        }

        When("the shortest already ends with .usecase") {
            Then("it returns it as-is") {
                inferUseCasePackageFrom(setOf("com.example.feature.usecase")) shouldBe
                    "com.example.feature.usecase"
            }
        }
    }
})
