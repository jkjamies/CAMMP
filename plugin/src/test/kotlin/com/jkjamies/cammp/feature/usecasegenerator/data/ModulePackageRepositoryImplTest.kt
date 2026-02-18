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

package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.PackageMetadataDataSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Path

/**
 * Tests for [ModulePackageRepositoryImpl].
 */
class ModulePackageRepositoryImplTest : BehaviorSpec({

    fun newRepo(): Pair<PackageMetadataDataSource, ModulePackageRepositoryImpl> {
        val ds = mockk<PackageMetadataDataSource>()
        return ds to ModulePackageRepositoryImpl(ds)
    }

    Given("ModulePackageRepositoryImpl") {
        val modulePath = Path.of("/path/to/module")

        When("data source returns a package") {
            Then("it should delegate and return it") {
                val (ds, repo) = newRepo()
                every { ds.findModulePackage(modulePath) } returns "com.example.domain.usecase"

                repo.findModulePackage(modulePath) shouldBe "com.example.domain.usecase"
            }
        }

        When("data source returns null") {
            Then("it should return null") {
                val (ds, repo) = newRepo()
                every { ds.findModulePackage(modulePath) } returns null

                repo.findModulePackage(modulePath) shouldBe null
            }
        }
    }
})
