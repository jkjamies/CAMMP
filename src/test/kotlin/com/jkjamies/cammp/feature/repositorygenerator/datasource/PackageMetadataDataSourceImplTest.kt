package com.jkjamies.cammp.feature.repositorygenerator.datasource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for [PackageMetadataDataSourceImpl].
 */
class PackageMetadataDataSourceImplTest : BehaviorSpec({

    Given("inferRepositoryModulePackageFrom") {

        When("package list is empty") {
            Then("it returns null") {
                inferRepositoryModulePackageFrom(emptySet(), moduleName = "data") shouldBe null
            }
        }

        When("moduleName is data and an exact .data package exists") {
            Then("it returns the exact match") {
                inferRepositoryModulePackageFrom(
                    packages = setOf(
                        "com.example.feature.data",
                        "com.example.feature.data.remote",
                        "com.example.feature.other",
                    ),
                    moduleName = "data",
                ) shouldBe "com.example.feature.data"
            }
        }

        When("moduleName is data and only a containing .data package exists") {
            Then("it truncates to .data") {
                inferRepositoryModulePackageFrom(
                    packages = setOf("com.example.feature.data.remote"),
                    moduleName = "data",
                ) shouldBe "com.example.feature.data"
            }
        }

        When("moduleName is data and multiple packages exist, it still prefers exact .data") {
            Then("it chooses the exact .data over shorter unrelated packages") {
                inferRepositoryModulePackageFrom(
                    packages = setOf(
                        "a",
                        "com.example.feature.data",
                    ),
                    moduleName = "data",
                ) shouldBe "com.example.feature.data"
            }
        }

        When("moduleName is domain and an exact .domain package exists") {
            Then("it returns the exact match") {
                inferRepositoryModulePackageFrom(
                    packages = setOf(
                        "com.example.feature.domain",
                        "com.example.feature.domain.model",
                    ),
                    moduleName = "domain",
                ) shouldBe "com.example.feature.domain"
            }
        }

        When("moduleName is di and only a containing .di package exists") {
            Then("it truncates to .di") {
                inferRepositoryModulePackageFrom(
                    packages = setOf("com.example.feature.di.modules"),
                    moduleName = "di",
                ) shouldBe "com.example.feature.di"
            }
        }

        When("moduleName is unknown") {
            Then("it returns the shortest package") {
                inferRepositoryModulePackageFrom(
                    packages = setOf(
                        "com.example.very.long.package.name",
                        "com.ex",
                    ),
                    moduleName = "whatever",
                ) shouldBe "com.ex"
            }
        }

        When("moduleName is null") {
            Then("it returns the shortest package") {
                inferRepositoryModulePackageFrom(
                    packages = setOf(
                        "com.example.longer",
                        "c",
                    ),
                    moduleName = null,
                ) shouldBe "c"
            }
        }
    }
})
