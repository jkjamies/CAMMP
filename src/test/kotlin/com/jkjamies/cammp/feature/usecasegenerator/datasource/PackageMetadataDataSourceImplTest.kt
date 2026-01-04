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
