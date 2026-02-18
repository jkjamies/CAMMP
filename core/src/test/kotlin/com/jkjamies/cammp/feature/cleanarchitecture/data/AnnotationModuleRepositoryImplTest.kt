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

import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain

/**
 * Tests for [AnnotationModuleRepositoryImpl].
 */
class AnnotationModuleRepositoryImplTest : BehaviorSpec({

    Given("AnnotationModuleRepositoryImpl") {
        val fs = FileSystemRepositoryImpl()
        val repo = AnnotationModuleRepositoryImpl(fs)

        When("generating a Koin annotations module") {
            Then("it should write a class with non-backticked annotation imports") {
                withTempDir("cammp_koin_module") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.feature.di",
                        scanPackage = "com.example.feature",
                        featureName = "profile",
                    )

                    val outFile = tmp.resolve("ProfileAnnotationsModule.kt")
                    fs.readText(outFile)!!.also { content ->
                        content shouldContain "package com.example.feature.di"
                        content shouldContain "class ProfileAnnotationsModule"
                        content shouldContain "@Module"
                        content shouldContain "@ComponentScan(\"com.example.feature\")"

                        // Ensure kotlinpoet's backticked import is normalized
                        content shouldContain "import org.koin.core.annotation.ComponentScan"
                        content shouldContain "import org.koin.core.annotation.Module"
                    }
                }
            }
        }

        When("featureName is already titlecase") {
            Then("it should keep the same capitalization") {
                withTempDir("cammp_koin_module2") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.feature.di",
                        scanPackage = "com.example.feature",
                        featureName = "Profile",
                    )

                    val outFile = tmp.resolve("ProfileAnnotationsModule.kt")
                    fs.readText(outFile)!!.also { content ->
                        content shouldContain "class ProfileAnnotationsModule"
                    }
                }
            }
        }
    }
})
