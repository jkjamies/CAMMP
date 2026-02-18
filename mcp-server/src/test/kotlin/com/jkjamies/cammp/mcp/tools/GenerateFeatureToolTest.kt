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

package com.jkjamies.cammp.mcp.tools

import com.jkjamies.cammp.domain.model.DatasourceStrategy
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import com.jkjamies.cammp.feature.cleanarchitecture.domain.usecase.CleanArchitectureGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Paths

class GenerateFeatureToolTest : BehaviorSpec({

    Given("parseDatasourceStrategy") {
        Then("parses all valid values") {
            parseDatasourceStrategy("none") shouldBe DatasourceStrategy.None
            parseDatasourceStrategy("combined") shouldBe DatasourceStrategy.Combined
            parseDatasourceStrategy("local_only") shouldBe DatasourceStrategy.LocalOnly
            parseDatasourceStrategy("remote_only") shouldBe DatasourceStrategy.RemoteOnly
            parseDatasourceStrategy("local_and_remote") shouldBe DatasourceStrategy.RemoteAndLocal
        }

        Then("is case-insensitive") {
            parseDatasourceStrategy("NONE") shouldBe DatasourceStrategy.None
            parseDatasourceStrategy("Combined") shouldBe DatasourceStrategy.Combined
        }

        Then("returns null for invalid values") {
            parseDatasourceStrategy("invalid") shouldBe null
            parseDatasourceStrategy("") shouldBe null
        }
    }

    Given("parseDiStrategy") {
        Then("parses all valid values") {
            parseDiStrategy("hilt") shouldBe DiStrategy.Hilt
            parseDiStrategy("koin") shouldBe DiStrategy.Koin(useAnnotations = false)
            parseDiStrategy("koin_annotations") shouldBe DiStrategy.Koin(useAnnotations = true)
            parseDiStrategy("metro") shouldBe DiStrategy.Metro
        }

        Then("is case-insensitive") {
            parseDiStrategy("HILT") shouldBe DiStrategy.Hilt
            parseDiStrategy("Metro") shouldBe DiStrategy.Metro
        }

        Then("returns null for invalid values") {
            parseDiStrategy("dagger") shouldBe null
            parseDiStrategy("") shouldBe null
        }
    }

    Given("handleGenerateFeature") {
        When("arguments are null") {
            Then("returns error") {
                val generator = mockk<CleanArchitectureGenerator>()
                val result = handleGenerateFeature(null, generator)
                result.isError shouldBe true
                (result.content.first() as TextContent).text shouldContain "No arguments provided"
            }
        }

        When("required arguments are missing") {
            val generator = mockk<CleanArchitectureGenerator>()

            Then("returns error for missing featureName") {
                val args = buildJsonObject {
                    put("projectPath", "/tmp/project")
                    put("packageName", "com.example")
                }
                val result = handleGenerateFeature(args, generator)
                result.isError shouldBe true
                (result.content.first() as TextContent).text shouldContain "required"
            }

            Then("returns error for missing projectPath") {
                val args = buildJsonObject {
                    put("featureName", "login")
                    put("packageName", "com.example")
                }
                val result = handleGenerateFeature(args, generator)
                result.isError shouldBe true
            }

            Then("returns error for missing packageName") {
                val args = buildJsonObject {
                    put("featureName", "login")
                    put("projectPath", "/tmp/project")
                }
                val result = handleGenerateFeature(args, generator)
                result.isError shouldBe true
            }
        }

        When("invalid strategy values are provided") {
            val generator = mockk<CleanArchitectureGenerator>()

            Then("returns error for invalid datasourceStrategy") {
                val args = buildJsonObject {
                    put("featureName", "login")
                    put("projectPath", "/tmp/project")
                    put("packageName", "com.example")
                    put("datasourceStrategy", "invalid")
                }
                val result = handleGenerateFeature(args, generator)
                result.isError shouldBe true
                (result.content.first() as TextContent).text shouldContain "datasourceStrategy"
            }

            Then("returns error for invalid diStrategy") {
                val args = buildJsonObject {
                    put("featureName", "login")
                    put("projectPath", "/tmp/project")
                    put("packageName", "com.example")
                    put("diStrategy", "dagger")
                }
                val result = handleGenerateFeature(args, generator)
                result.isError shouldBe true
                (result.content.first() as TextContent).text shouldContain "diStrategy"
            }
        }

        When("valid arguments are provided") {
            Then("passes correct params to generator") {
                val paramsSlot = slot<CleanArchitectureParams>()
                val generator = mockk<CleanArchitectureGenerator>()
                coEvery { generator.invoke(capture(paramsSlot)) } returns Result.success(
                    CleanArchitectureResult(
                        created = listOf(":feature:login:domain", ":feature:login:data"),
                        skipped = emptyList(),
                        settingsUpdated = true,
                        buildLogicCreated = false,
                        message = "Done",
                    )
                )

                val args = buildJsonObject {
                    put("featureName", "login")
                    put("projectPath", "/tmp/project")
                    put("packageName", "com.example.app")
                    put("rootModule", "features")
                    put("datasourceStrategy", "local_and_remote")
                    put("diStrategy", "metro")
                    put("includePresentation", false)
                    put("includeApiModule", true)
                    put("includeDiModule", false)
                }

                val result = handleGenerateFeature(args, generator)

                result.isError shouldBe null
                with(paramsSlot.captured) {
                    projectBasePath shouldBe Paths.get("/tmp/project")
                    root shouldBe "features"
                    feature shouldBe "login"
                    orgCenter shouldBe "com.example.app"
                    datasourceStrategy shouldBe DatasourceStrategy.RemoteAndLocal
                    diStrategy shouldBe DiStrategy.Metro
                    includePresentation shouldBe false
                    includeApiModule shouldBe true
                    includeDiModule shouldBe false
                }

                val text = (result.content.first() as TextContent).text
                text shouldContain "login"
                text shouldContain ":feature:login:domain"
                text shouldContain "settings.gradle.kts updated"
            }

            Then("uses defaults for optional arguments") {
                val paramsSlot = slot<CleanArchitectureParams>()
                val generator = mockk<CleanArchitectureGenerator>()
                coEvery { generator.invoke(capture(paramsSlot)) } returns Result.success(
                    CleanArchitectureResult(
                        created = emptyList(),
                        skipped = emptyList(),
                        settingsUpdated = false,
                        buildLogicCreated = false,
                        message = "",
                    )
                )

                val args = buildJsonObject {
                    put("featureName", "login")
                    put("projectPath", "/tmp/project")
                    put("packageName", "com.example")
                }

                handleGenerateFeature(args, generator)

                with(paramsSlot.captured) {
                    root shouldBe "app"
                    datasourceStrategy shouldBe DatasourceStrategy.None
                    diStrategy shouldBe DiStrategy.Hilt
                    includePresentation shouldBe true
                    includeApiModule shouldBe false
                    includeDiModule shouldBe true
                }
            }
        }

        When("generator fails") {
            Then("returns error result") {
                val generator = mockk<CleanArchitectureGenerator>()
                coEvery { generator.invoke(any()) } returns Result.failure(
                    RuntimeException("Generation failed")
                )

                val args = buildJsonObject {
                    put("featureName", "login")
                    put("projectPath", "/tmp/project")
                    put("packageName", "com.example")
                }

                val result = handleGenerateFeature(args, generator)
                result.isError shouldBe true
                (result.content.first() as TextContent).text shouldContain "Generation failed"
            }
        }
    }

    Given("GENERATE_FEATURE_TOOL definition") {
        Then("has correct name and required fields") {
            GENERATE_FEATURE_TOOL.name shouldBe "generate_feature"
            GENERATE_FEATURE_TOOL.inputSchema.required shouldBe listOf("featureName", "projectPath", "packageName")
        }
    }
})
