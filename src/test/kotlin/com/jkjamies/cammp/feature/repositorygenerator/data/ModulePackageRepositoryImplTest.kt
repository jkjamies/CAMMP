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

package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.data.datasource.PackageMetadataDataSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Paths

/**
 * Tests for [ModulePackageRepositoryImpl].
 */
class ModulePackageRepositoryImplTest : BehaviorSpec({

    Given("ModulePackageRepositoryImpl") {

        When("findModulePackage is called and datasource returns a package") {
            Then("it delegates and returns the package") {
                val dataSource = mockk<PackageMetadataDataSource>()
                val repository = ModulePackageRepositoryImpl(dataSource)

                val path = Paths.get("some/path")
                every { dataSource.findModulePackage(path) } returns "com.example"

                val result = repository.findModulePackage(path)
                result shouldBe "com.example"
            }
        }

        When("findModulePackage is called and datasource returns null") {
            Then("it throws an error") {
                val dataSource = mockk<PackageMetadataDataSource>()
                val repository = ModulePackageRepositoryImpl(dataSource)

                val path = Paths.get("some/path")
                every { dataSource.findModulePackage(path) } returns null

                shouldThrow<IllegalStateException> {
                    repository.findModulePackage(path)
                }
            }
        }
    }
})