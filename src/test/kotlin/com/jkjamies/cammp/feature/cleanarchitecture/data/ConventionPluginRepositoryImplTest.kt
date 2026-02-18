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

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.PluginType
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.shouldBe
import java.nio.file.Files

/**
 * Tests for [ConventionPluginRepositoryImpl].
 */
class ConventionPluginRepositoryImplTest : BehaviorSpec({

    Given("ConventionPluginRepositoryImpl") {
        val fs = FileSystemRepositoryImpl()
        val repo = ConventionPluginRepositoryImpl(fs)

        When("generating presentation plugin with Hilt") {
            Then("it should include compose + hilt navigation dependencies") {
                withTempDir("cammp_convention") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.convention",
                        diMode = DiMode.HILT,
                        type = PluginType.PRESENTATION,
                    )

                    val out = tmp.resolve("PresentationConventionPlugin.kt")
                    fs.readText(out)!!.also { content ->
                        content shouldContain "package com.example.convention"
                        content shouldContain "class PresentationConventionPlugin"
                        content shouldContain "compose = true"
                        // plugin application
                        content shouldContain "Aliases.PluginAliases.COMPOSE_COMPILER"
                        content shouldContain "Aliases.PluginAliases.HILT"
                        // deps
                        content shouldContain "LibsCompose.HILT_NAVIGATION"
                    }
                }
            }
        }

        When("generating data plugin with Koin") {
            Then("it should not include Hilt-specific parts") {
                withTempDir("cammp_convention2") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.convention",
                        diMode = DiMode.KOIN,
                        type = PluginType.DATA,
                    )

                    val out = tmp.resolve("DataConventionPlugin.kt")
                    fs.readText(out)!!.also { content ->
                        content shouldContain "class DataConventionPlugin"
                        // should apply base android + kotlin plugins
                        content shouldContain "Aliases.PluginAliases.ANDROID_LIBRARY"
                        content shouldContain "Aliases.PluginAliases.KOTLIN_ANDROID"
                        // should not apply hilt plugin
                        content.contains("Aliases.PluginAliases.HILT") shouldBe false
                        // should not include compose compiler
                        content.contains("Aliases.PluginAliases.COMPOSE_COMPILER") shouldBe false
                    }
                }
            }
        }

        When("generating remote datasource plugin") {
            Then("it should create the correct file name") {
                withTempDir("cammp_convention3") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.convention",
                        diMode = DiMode.HILT,
                        type = PluginType.REMOTE_DATA_SOURCE,
                    )

                    val out = tmp.resolve("RemoteDataSourceConventionPlugin.kt")
                    fs.readText(out)!!.also { content ->
                        content shouldContain "class RemoteDataSourceConventionPlugin"
                    }
                }
            }
        }

        When("generating presentation plugin with Metro") {
            Then("it should apply Metro plugin but NOT Hilt dependencies") {
                withTempDir("cammp_convention_metro") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.convention",
                        diMode = DiMode.METRO,
                        type = PluginType.PRESENTATION,
                    )

                    val out = tmp.resolve("PresentationConventionPlugin.kt")
                    fs.readText(out)!!.also { content ->
                        content shouldContain "package com.example.convention"
                        content shouldContain "class PresentationConventionPlugin"

                        // Metro Plugin
                        content shouldContain "Aliases.PluginAliases.METRO"

                        // NO Hilt Plugin
                        content.contains("Aliases.PluginAliases.HILT") shouldBe false
                        // NO Hilt Dependencies
                        content.contains("LibsCompose.HILT_NAVIGATION") shouldBe false
                    }
                }
            }
        }
    }
})
