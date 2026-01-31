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

import com.jkjamies.cammp.feature.cleanarchitecture.data.datasource.VersionCatalogDataSource
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Path

/**
 * Tests for [AliasesRepositoryImpl].
 */
class AliasesRepositoryImplTest : BehaviorSpec({

    Given("AliasesRepositoryImpl") {
        val fs = FileSystemRepositoryImpl()

        val versionCatalog = object : VersionCatalogDataSource {
            override fun getLibraryAlias(
                tomlPath: Path,
                alias: String,
                group: String,
                artifact: String,
                version: String?,
                versionRef: String?
            ): String = "lib.$alias"

            override fun getPluginAlias(
                tomlPath: Path,
                alias: String,
                id: String,
                version: String?,
                versionRef: String?
            ): String = "plugin.$alias"
        }

        val repo = AliasesRepositoryImpl(fs, versionCatalog)

        When("generating aliases for Hilt") {
            Then("it should write Aliases.kt with expected sections") {
                withTempDir("cammp_aliases_hilt") { outDir ->
                    repo.generateAliases(
                        outputDirectory = outDir,
                        packageName = "com.example.convention.core",
                        diMode = DiMode.HILT,
                        tomlPath = outDir.resolve("libs.versions.toml"),
                    )

                    val out = outDir.resolve("Aliases.kt")
                    fs.readText(out)!!.also { content ->
                        content shouldContain "package com.example.convention.core"
                        content shouldContain "internal object Aliases"
                        content shouldContain "internal object PluginAliases"
                        content shouldContain "internal object BuildPropAliases"
                        content shouldContain "internal object Dependencies"

                        // Hilt/KSP expected keys
                        content shouldContain "const val HILT"
                        content shouldContain "const val KSP"
                    }
                }
            }
        }

        When("generating aliases for plain Koin") {
            Then("it should not include hilt plugin alias") {
                withTempDir("cammp_aliases_koin") { outDir ->
                    repo.generateAliases(
                        outputDirectory = outDir,
                        packageName = "com.example.convention.core",
                        diMode = DiMode.KOIN,
                        tomlPath = outDir.resolve("libs.versions.toml"),
                    )

                    val out = outDir.resolve("Aliases.kt")
                    fs.readText(out)!!.also { content ->
                        // should have KOIN libs but not HILT plugin alias
                        content shouldContain "KOIN"
                        content.contains("const val HILT") shouldBe false
                    }
                }
            }
        }
    }
})
