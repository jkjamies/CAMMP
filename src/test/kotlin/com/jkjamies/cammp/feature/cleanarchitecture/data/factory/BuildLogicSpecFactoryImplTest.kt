package com.jkjamies.cammp.feature.cleanarchitecture.data.factory

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain

/**
 * Tests for [BuildLogicSpecFactoryImpl].
 */
class BuildLogicSpecFactoryImplTest : BehaviorSpec({

    Given("BuildLogicSpecFactoryImpl") {
        val factory = BuildLogicSpecFactoryImpl()

        When("applying package tokens") {
            val out = factory.applyPackageTokens(
                rawTemplate = """
                    package ${'$'}{PACKAGE}

                    // fully-qualified
                    val x = \"com.PACKAGE.foo\"

                    // path
                    val p = \"com/PACKAGE/foo\"

                    // raw token
                    val y = \"PACKAGE\"
                """.trimIndent(),
                orgCenter = "com.Example-Org",
            )

            Then("it should replace tokens consistently") {
                out.shouldContain("package exampleOrg")
                out.shouldContain("com.exampleOrg.foo")
                out.shouldContain("com/exampleOrg/foo")
            }
        }

        When("orgCenter is blank") {
            val out = factory.applyPackageTokens(
                rawTemplate = "package ${'$'}{PACKAGE}",
                orgCenter = "   ",
            )

            Then("it should fall back to cammp") {
                out.shouldContain("package cammp")
            }
        }

        When("orgCenter is wrapped like \"\${com.example}\"") {
            val out = factory.applyPackageTokens(
                rawTemplate = "package ${'$'}{PACKAGE}",
                orgCenter = "${'$'}{com.example}",
            )

            Then("it should unwrap and normalize") {
                out.shouldContain("package example")
            }
        }
    }
})
