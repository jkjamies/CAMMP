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

package com.jkjamies.cammp.feature.cleanarchitecture.data.datasource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path

/**
 * Tests for the [VersionCatalogDataSource] contract.
 *
 * This is a lightweight “contract test” that intentionally only verifies the interface
 * can be implemented and called in unit tests.
 */
class VersionCatalogDataSourceTest : BehaviorSpec({

    Given("a VersionCatalogDataSource implementation") {
        val ds = object : VersionCatalogDataSource {
            override fun getLibraryAlias(
                tomlPath: Path,
                alias: String,
                group: String,
                artifact: String,
                version: String?,
                versionRef: String?
            ): String = "lib:$alias:$group:$artifact:${version ?: versionRef ?: ""}"

            override fun getPluginAlias(
                tomlPath: Path,
                alias: String,
                id: String,
                version: String?,
                versionRef: String?
            ): String = "plugin:$alias:$id:${version ?: versionRef ?: ""}"
        }

        When("requesting a library alias") {
            Then("it returns a stable string") {
                ds.getLibraryAlias(
                    tomlPath = Path.of("/project/gradle/libs.versions.toml"),
                    alias = "kotlinPoet",
                    group = "com.squareup",
                    artifact = "kotlinpoet",
                    version = "1.0.0",
                ) shouldBe "lib:kotlinPoet:com.squareup:kotlinpoet:1.0.0"
            }
        }

        When("requesting a plugin alias") {
            Then("it returns a stable string") {
                ds.getPluginAlias(
                    tomlPath = Path.of("/project/gradle/libs.versions.toml"),
                    alias = "kotlinSerialization",
                    id = "org.jetbrains.kotlin.plugin.serialization",
                    versionRef = "kotlin",
                ) shouldBe "plugin:kotlinSerialization:org.jetbrains.kotlin.plugin.serialization:kotlin"
            }
        }
    }
})

