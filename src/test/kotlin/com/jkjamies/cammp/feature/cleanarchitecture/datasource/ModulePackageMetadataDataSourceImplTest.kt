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
import java.nio.file.Path

/**
 * Tests for [ModulePackageMetadataDataSourceImpl].
 */
class ModulePackageMetadataDataSourceImplTest : BehaviorSpec({

    Given("ModulePackageMetadataDataSourceImpl") {
        val moduleDir = Path.of("/project/feature/profile/domain")
        val kotlinRoot = moduleDir.resolve("src/main/kotlin")
        val placeholder = kotlinRoot.resolve("Placeholder.kt")

        When("src/main/kotlin is missing") {
            val fs = FileSystemRepositoryFake().apply {
                markExisting(moduleDir, isDir = true)
            }
            val ds = ModulePackageMetadataDataSourceImpl(fs)

            Then("it should return null") {
                ds.findModulePackage(moduleDir) shouldBe null
            }
        }

        When("Placeholder.kt contains a package declaration") {
            val fs = FileSystemRepositoryFake().apply {
                markExisting(moduleDir, isDir = true)
                markExisting(kotlinRoot, isDir = true)
                markExisting(
                    placeholder,
                    content = """
                        package com.example.feature.profile.domain

                        class Placeholder
                    """.trimIndent(),
                )
            }
            val ds = ModulePackageMetadataDataSourceImpl(fs)

            Then("it should parse and return the package") {
                ds.findModulePackage(moduleDir) shouldBe "com.example.feature.profile.domain"
            }
        }

        When("Placeholder.kt does not exist") {
            val fs = FileSystemRepositoryFake().apply {
                markExisting(moduleDir, isDir = true)
                markExisting(kotlinRoot, isDir = true)
            }
            val ds = ModulePackageMetadataDataSourceImpl(fs)

            Then("it should return null") {
                ds.findModulePackage(moduleDir) shouldBe null
            }
        }
    }
})

