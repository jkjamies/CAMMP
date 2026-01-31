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
