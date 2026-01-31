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

import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import com.jkjamies.cammp.feature.repositorygenerator.data.factory.DataSourceSpecFactory
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Tests for [DatasourceScaffoldRepositoryImpl].
 */
class DatasourceScaffoldRepositoryImplTest : BehaviorSpec({

    Given("DatasourceScaffoldRepositoryImpl") {

        When("generateInterface is called") {
            Then("it creates directories, calls the spec factory, and writes <className>.kt") {
                withTempDir("scaffold_interface_test") { tempDir ->
                    val specFactory = mockk<DataSourceSpecFactory>()
                    val repository = DatasourceScaffoldRepositoryImpl(specFactory)

                    val packageName = "com.example"
                    val className = "UserDataSource"

                    val dummySpec = FileSpec.builder(packageName, className)
                        .addType(TypeSpec.interfaceBuilder(className).build())
                        .build()

                    every { specFactory.createInterface(packageName, className) } returns dummySpec

                    val result = repository.generateInterface(tempDir, packageName, className)

                    result.exists() shouldBe true
                    result.toString() shouldBe tempDir.resolve("$className.kt").toString()
                    result.readText() shouldBe dummySpec.toString()

                    verify(exactly = 1) { specFactory.createInterface(packageName, className) }
                }
            }
        }

        When("generateImplementation is called") {
            Then("it creates directories, calls the spec factory, and writes <className>.kt") {
                withTempDir("scaffold_impl_test") { tempDir ->
                    val specFactory = mockk<DataSourceSpecFactory>()
                    val repository = DatasourceScaffoldRepositoryImpl(specFactory)

                    val packageName = "com.example"
                    val className = "UserDataSourceImpl"
                    val interfacePackage = "com.example"
                    val interfaceName = "UserDataSource"
                    val useKoin = true

                    val dummySpec = FileSpec.builder(packageName, className)
                        .addType(TypeSpec.classBuilder(className).build())
                        .build()

                    every {
                        specFactory.createImplementation(
                            packageName = packageName,
                            className = className,
                            interfacePackage = interfacePackage,
                            interfaceName = interfaceName,
                            useKoin = useKoin,
                        )
                    } returns dummySpec

                    val result = repository.generateImplementation(
                        directory = tempDir,
                        packageName = packageName,
                        className = className,
                        interfacePackage = interfacePackage,
                        interfaceName = interfaceName,
                        useKoin = useKoin,
                    )

                    result.exists() shouldBe true
                    result.toString() shouldBe tempDir.resolve("$className.kt").toString()
                    result.readText() shouldBe dummySpec.toString()

                    verify(exactly = 1) {
                        specFactory.createImplementation(
                            packageName = packageName,
                            className = className,
                            interfacePackage = interfacePackage,
                            interfaceName = interfaceName,
                            useKoin = useKoin,
                        )
                    }
                }
            }
        }
    }
})