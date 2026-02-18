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

package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.data.datasource.PackageMetadataDataSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.nio.file.Path

/**
 * Tests for [ModulePackageRepositoryImpl].
 */
class ModulePackageRepositoryImplTest : BehaviorSpec({

    // Factory returns *fresh* instances per call so it remains safe under spec/test concurrency.
    fun newRepo(): Pair<PackageMetadataDataSource, ModulePackageRepositoryImpl> {
        val ds = mockk<PackageMetadataDataSource>()
        return ds to ModulePackageRepositoryImpl(ds)
    }

    Given("ModulePackageRepositoryImpl") {

        When("data source returns a package") {
            Then("it should return it and forward the moduleDir") {
                val (ds, repo) = newRepo()

                val dir = Path.of("/tmp/module")
                every { ds.findModulePackage(dir) } returns "com.example.presentation"

                repo.findModulePackage(dir) shouldBe "com.example.presentation"
                verify(exactly = 1) { ds.findModulePackage(dir) }
            }
        }

        When("data source returns null") {
            Then("it should return null") {
                val (ds, repo) = newRepo()

                val dir = Path.of("/tmp/module")
                every { ds.findModulePackage(dir) } returns null

                repo.findModulePackage(dir) shouldBe null
                verify(exactly = 1) { ds.findModulePackage(dir) }
            }
        }
    }
})
