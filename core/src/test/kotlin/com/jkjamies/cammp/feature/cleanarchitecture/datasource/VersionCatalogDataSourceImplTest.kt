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

package com.jkjamies.cammp.feature.cleanarchitecture.datasource

import com.jkjamies.cammp.feature.cleanarchitecture.testutil.FileSystemRepositoryFake
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Path

/**
 * Tests for [VersionCatalogDataSourceImpl].
 */
class VersionCatalogDataSourceImplTest : BehaviorSpec({

    Given("VersionCatalogDataSourceImpl") {
        val tomlPath = Path.of("/project/gradle/libs.versions.toml")

        When("the catalog is empty") {
            val fs = FileSystemRepositoryFake().apply {
                markExisting(
                    tomlPath,
                    content = """
                        [versions]

                        [libraries]

                        [plugins]
                    """.trimIndent(),
                )
            }
            val ds = VersionCatalogDataSourceImpl(fs)

            val alias = ds.getLibraryAlias(
                tomlPath = tomlPath,
                alias = "my-lib",
                group = "com.example",
                artifact = "mylib",
                version = "1.0.0",
                versionRef = null,
            )

            Then("it should return the requested alias") {
                alias shouldBe "my-lib"
            }

            Then("it should write versions + libraries sections") {
                val text = fs.writes[tomlPath] ?: ""
                text.shouldContain("[versions]")
                text.shouldContain("my-lib = \"1.0.0\"")
                text.shouldContain("[libraries]")
                text.shouldContain("my-lib = { group = \"com.example\", name = \"mylib\", version.ref = \"my-lib\" }")
            }
        }

        When("a library already exists") {
            val fs = FileSystemRepositoryFake().apply {
                markExisting(
                    tomlPath,
                    content = """
                        [libraries]
                        existing = { group = "com.example", name = "mylib" }
                    """.trimIndent(),
                )
            }
            val ds = VersionCatalogDataSourceImpl(fs)

            val alias = ds.getLibraryAlias(
                tomlPath = tomlPath,
                alias = "my-lib",
                group = "com.example",
                artifact = "mylib",
                version = null,
                versionRef = null,
            )

            Then("it should return the existing alias") {
                alias shouldBe "existing"
            }
        }

        When("a plugin already exists") {
            val fs = FileSystemRepositoryFake().apply {
                markExisting(
                    tomlPath,
                    content = """
                        [plugins]
                        android-app = { id = "com.android.application" }
                    """.trimIndent(),
                )
            }
            val ds = VersionCatalogDataSourceImpl(fs)

            val alias = ds.getPluginAlias(
                tomlPath = tomlPath,
                alias = "x",
                id = "com.android.application",
                version = null,
                versionRef = null,
            )

            Then("it should return the existing alias") {
                alias shouldBe "android-app"
            }
        }

        When("a plugin does not exist") {
            Then("it should add the plugin entry and return the requested alias") {
                val fs = FileSystemRepositoryFake().apply {
                    // include an extra unknown section to cover writeToml's secondary section loop
                    markExisting(
                        tomlPath,
                        content = """
                            [extra]
                            foo = "bar"

                            [plugins]
                        """.trimIndent(),
                    )
                }
                val ds = VersionCatalogDataSourceImpl(fs)

                val alias = ds.getPluginAlias(
                    tomlPath = tomlPath,
                    alias = "android-app",
                    id = "com.android.application",
                    version = null,
                    versionRef = null,
                )

                alias shouldBe "android-app"

                val text = fs.writes[tomlPath] ?: ""
                text.shouldContain("[plugins]")
                text.shouldContain("android-app = { id = \"com.android.application\" }")
                // extra section should still be preserved
                text.shouldContain("[extra]")
                text.shouldContain("foo = \"bar\"")
            }
        }

        When("a plugin does not exist and a version is provided") {
            Then("it should add a version entry and reference it") {
                val fs = FileSystemRepositoryFake().apply {
                    markExisting(Path.of("/project"), isDir = true)
                    markExisting(tomlPath, content = """
                        [plugins]
                    """.trimIndent())
                }
                val ds = VersionCatalogDataSourceImpl(fs)

                val alias = ds.getPluginAlias(
                    tomlPath = tomlPath,
                    alias = "kotlinSerialization",
                    id = "org.jetbrains.kotlin.plugin.serialization",
                    version = "2.2.0",
                    versionRef = "kotlin",
                )

                alias shouldBe "kotlinSerialization"

                val text = fs.writes[tomlPath] ?: ""
                text.shouldContain("[versions]")
                text.shouldContain("kotlin = \"2.2.0\"")
                text.shouldContain("[plugins]")
                text.shouldContain("kotlinSerialization = { id = \"org.jetbrains.kotlin.plugin.serialization\", version.ref = \"kotlin\" }")
            }
        }

        When("the toml path does not exist") {
            Then("it should return alias and not write") {
                val fs = FileSystemRepositoryFake() // does not markExisting(tomlPath)
                val ds = VersionCatalogDataSourceImpl(fs)

                val alias = ds.getPluginAlias(
                    tomlPath = tomlPath,
                    alias = "x",
                    id = "com.example.plugin",
                    version = null,
                    versionRef = null,
                )

                alias shouldBe "x"
                fs.writes.containsKey(tomlPath) shouldBe false
            }
        }

        When("a library is declared using compact notation") {
            Then("it should detect and return the existing alias") {
                val fs = FileSystemRepositoryFake().apply {
                    markExisting(
                        tomlPath,
                        content = """
                            [libraries]
                            compact = "com.example:mylib"
                        """.trimIndent(),
                    )
                }
                val ds = VersionCatalogDataSourceImpl(fs)

                val alias = ds.getLibraryAlias(
                    tomlPath = tomlPath,
                    alias = "ignored",
                    group = "com.example",
                    artifact = "mylib",
                    version = "1.0.0",
                    versionRef = null,
                )

                alias shouldBe "compact"
                (fs.writes[tomlPath] ?: "") shouldNotContain "ignored ="
            }
        }
    }
})
