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

package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists

/**
 * Tests for [sanitizeScreenName], [inferPresentationPackage], [deriveModuleName], and [resolveScreenDir].
 */
class PresentationStepUtilsTest : BehaviorSpec({

    Given("sanitizeScreenName") {
        When("input is a normal capitalized name") {
            Then("it should return it unchanged") {
                sanitizeScreenName("Home") shouldBe "Home"
            }
        }

        When("input starts with lowercase") {
            Then("it should capitalize the first character") {
                sanitizeScreenName("home") shouldBe "Home"
            }
        }

        When("input contains special characters") {
            Then("it should strip them and capitalize") {
                sanitizeScreenName("my-screen!") shouldBe "Myscreen"
            }
        }

        When("input is empty") {
            Then("it should return Screen") {
                sanitizeScreenName("") shouldBe "Screen"
            }
        }

        When("input is whitespace only") {
            Then("it should return Screen") {
                sanitizeScreenName("   ") shouldBe "Screen"
            }
        }

        When("input contains numbers") {
            Then("it should preserve them") {
                sanitizeScreenName("Screen2") shouldBe "Screen2"
            }
        }
    }

    Given("deriveModuleName") {
        When("package contains presentation segment") {
            Then("it should return the segment before presentation capitalized") {
                deriveModuleName("com.example.feature.presentation") shouldBe "Feature"
            }
        }

        When("package does not contain presentation") {
            Then("it should return the last segment capitalized") {
                deriveModuleName("com.example.app") shouldBe "App"
            }
        }

        When("presentation is at index 0") {
            Then("it should fall back to last segment") {
                deriveModuleName("presentation") shouldBe "Presentation"
            }
        }

        When("package has single non-presentation segment") {
            Then("it should return it capitalized") {
                deriveModuleName("mymodule") shouldBe "Mymodule"
            }
        }
    }

    Given("inferPresentationPackage") {
        When("repository returns a package") {
            Then("it should use it") {
                val repo = object : ModulePackageRepository {
                    override fun findModulePackage(moduleDir: Path): String = "com.example.feature"
                }
                inferPresentationPackage(repo, Path.of("/tmp")) shouldBe "com.example.feature"
            }
        }

        When("repository returns null") {
            Then("it should fall back to com.example.presentation") {
                val repo = object : ModulePackageRepository {
                    override fun findModulePackage(moduleDir: Path): String? = null
                }
                inferPresentationPackage(repo, Path.of("/tmp")) shouldBe "com.example.presentation"
            }
        }
    }

    Given("resolveScreenDir") {
        When("called with valid params") {
            Then("it should create directories and return correct setup") {
                val tempDir = createTempDirectory("step_utils_test")
                try {
                    val repo = object : ModulePackageRepository {
                        override fun findModulePackage(moduleDir: Path): String = "com.example.home"
                    }
                    val params = PresentationParams(
                        moduleDir = tempDir,
                        screenName = "Dashboard",
                        patternStrategy = PresentationPatternStrategy.MVI,
                        diStrategy = DiStrategy.Hilt,
                    )

                    val setup = resolveScreenDir(repo, params)

                    setup.sanitizedName shouldBe "Dashboard"
                    setup.basePkg shouldBe "com.example.home"
                    setup.screenPackage shouldBe "com.example.home.dashboard"
                    setup.targetDir.exists() shouldBe true
                    setup.targetDir.toString() shouldEndWith "com/example/home/dashboard"
                } finally {
                    tempDir.toFile().deleteRecursively()
                }
            }
        }
    }
})
