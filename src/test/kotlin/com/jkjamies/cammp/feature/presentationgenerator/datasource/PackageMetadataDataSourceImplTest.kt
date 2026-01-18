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
