package com.jkjamies.cammp.feature.cleanarchitecture.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Test class for [GradleSettingsRepositoryImpl].
 */
class GradleSettingsRepositoryImplTest : BehaviorSpec({

    Given("a GradleSettingsRepositoryImpl") {
        val repository = GradleSettingsRepositoryImpl()
        val tempDir = createTempDirectory("test_gradle_settings")

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("ensureIncludes is called") {
            val settingsFile = tempDir.resolve("settings.gradle.kts")

            And("settings file does not exist") {
                Then("it should return false") {
                    repository.ensureIncludes(tempDir, "", "feature", listOf("module")) shouldBe false
                }
            }

            And("settings file exists") {
                settingsFile.writeText("// settings")

                Then("it should add include statements") {
                    val result = repository.ensureIncludes(tempDir, "", "feature", listOf("module1", "module2"))
                    result shouldBe true
                    val content = settingsFile.readText()
                    content shouldContain "include(\":feature:module1\")"
                    content shouldContain "include(\":feature:module2\")"
                }

                Then("it should not duplicate include statements") {
                    val result = repository.ensureIncludes(tempDir, "", "feature", listOf("module1"))
                    result shouldBe false
                    val content = settingsFile.readText()
                    content.lines().count { it.contains("include(\":feature:module1\")") } shouldBe 1
                }

                Then("it should handle root path correctly") {
                    repository.ensureIncludes(tempDir, "core", "network", listOf("api"))
                    val content = settingsFile.readText()
                    content shouldContain "include(\":core:network:api\")"
                }
            }
        }

        When("ensureIncludeBuild is called") {
            val settingsFile = tempDir.resolve("settings.gradle.kts")
            if (!settingsFile.exists()) settingsFile.writeText("// settings")

            Then("it should add includeBuild statement") {
                val result = repository.ensureIncludeBuild(tempDir, "build-logic")
                result shouldBe true
                settingsFile.readText() shouldContain "includeBuild(\"build-logic\")"
            }

            Then("it should not duplicate includeBuild statement") {
                val result = repository.ensureIncludeBuild(tempDir, "build-logic")
                result shouldBe false
            }
        }

        When("ensureVersionCatalogPluginAliases is called") {
            val gradleDir = tempDir.resolve("gradle")
            val catalogFile = gradleDir.resolve("libs.versions.toml")

            And("catalog file does not exist") {
                Then("it should create catalog file with aliases") {
                    val result = repository.ensureVersionCatalogPluginAliases(tempDir, "myorg", listOf("domain", "data"))
                    result shouldBe true
                    catalogFile.exists() shouldBe true
                    val content = catalogFile.readText()
                    content shouldContain "convention-android-library-domain = { id = \"com.myorg.convention.android.library.domain\" }"
                    content shouldContain "convention-android-library-data = { id = \"com.myorg.convention.android.library.data\" }"
                }
            }

            And("catalog file exists") {
                Then("it should append aliases to plugins section") {
                    val result = repository.ensureVersionCatalogPluginAliases(tempDir, "myorg", listOf("presentation"))
                    result shouldBe true
                    val content = catalogFile.readText()
                    content shouldContain "convention-android-library-presentation = { id = \"com.myorg.convention.android.library.presentation\" }"
                }

                Then("it should not duplicate aliases") {
                    val result = repository.ensureVersionCatalogPluginAliases(tempDir, "myorg", listOf("presentation"))
                    result shouldBe false
                }
            }
        }

        When("ensureAppDependency is called") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")

            And("build file does not exist") {
                Then("it should return false") {
                    repository.ensureAppDependency(tempDir, "", "feature") shouldBe false
                }
            }

            And("build file exists without dependencies block") {
                buildFile.writeText("// build file")

                Then("it should add dependencies block and dependency") {
                    val result = repository.ensureAppDependency(tempDir, "", "feature")
                    result shouldBe true
                    val content = buildFile.readText()
                    content shouldContain "dependencies {"
                    content shouldContain "implementation(project(\":feature:di\"))"
                }
            }

            And("build file exists with dependencies block") {
                buildFile.writeText("dependencies {\n    implementation(\"something\")\n}")

                Then("it should add dependency to existing block") {
                    val result = repository.ensureAppDependency(tempDir, "", "otherFeature")
                    result shouldBe true
                    val content = buildFile.readText()
                    content shouldContain "implementation(project(\":otherFeature:di\"))"
                }

                Then("it should not duplicate dependency") {
                    val result = repository.ensureAppDependency(tempDir, "", "otherFeature")
                    result shouldBe false
                }
            }
        }
    }
})
