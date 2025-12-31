package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
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

                Then("it should add include statements and handle idempotency") {
                    // First call: adds includes
                    val result1 = repository.ensureIncludes(tempDir, "", "feature", listOf("module1", "module2"))
                    result1 shouldBe true
                    var content = settingsFile.readText()
                    content shouldContain "include(\":feature:module1\")"
                    content shouldContain "include(\":feature:module2\")"

                    // Second call: should not duplicate
                    val result2 = repository.ensureIncludes(tempDir, "", "feature", listOf("module1"))
                    result2 shouldBe false
                    content = settingsFile.readText()
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

            Then("it should add includeBuild statement and handle idempotency") {
                // First call
                val result1 = repository.ensureIncludeBuild(tempDir, "build-logic")
                result1 shouldBe true
                settingsFile.readText() shouldContain "includeBuild(\"build-logic\")"

                // Second call
                val result2 = repository.ensureIncludeBuild(tempDir, "build-logic")
                result2 shouldBe false
                settingsFile.readText().lines().count { it.contains("includeBuild(\"build-logic\")") } shouldBe 1
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
                Then("it should append aliases to plugins section and handle idempotency") {
                    // First call
                    val result1 = repository.ensureVersionCatalogPluginAliases(tempDir, "myorg", listOf("presentation"))
                    result1 shouldBe true
                    var content = catalogFile.readText()
                    content shouldContain "convention-android-library-presentation = { id = \"com.myorg.convention.android.library.presentation\" }"

                    // Second call
                    val result2 = repository.ensureVersionCatalogPluginAliases(tempDir, "myorg", listOf("presentation"))
                    result2 shouldBe false
                    content = catalogFile.readText()
                    content.lines().count { it.contains("convention-android-library-presentation") } shouldBe 1
                }
            }
        }

        When("ensureAppDependency is called") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")

            And("build file does not exist") {
                Then("it should return false") {
                    repository.ensureAppDependency(tempDir, "", "feature", DiMode.KOIN) shouldBe false
                }
            }

            And("build file exists without dependencies block") {
                buildFile.writeText("// build file")

                Then("it should add dependencies block and dependency") {
                    val result = repository.ensureAppDependency(tempDir, "", "feature", DiMode.KOIN)
                    result shouldBe true
                    val content = buildFile.readText()
                    content shouldContain "dependencies {"
                    content shouldContain "implementation(project(\":feature:di\"))"
                }
            }

            And("build file exists with dependencies block") {
                buildFile.writeText("dependencies {\n    implementation(\"something\")\n}")

                Then("it should add dependency to existing block and handle idempotency") {
                    // First call
                    val result1 = repository.ensureAppDependency(tempDir, "", "otherFeature", DiMode.KOIN)
                    result1 shouldBe true
                    var content = buildFile.readText()
                    content shouldContain "implementation(project(\":otherFeature:di\"))"

                    // Second call
                    val result2 = repository.ensureAppDependency(tempDir, "", "otherFeature", DiMode.KOIN)
                    result2 shouldBe false
                    content = buildFile.readText()
                    content.lines().count { it.contains("implementation(project(\":otherFeature:di\"))") } shouldBe 1
                }
            }
        }

        When("ensureAppDependency is called with Hilt") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            catalogFile.writeText("[versions]\n[libraries]\n")

            val result = repository.ensureAppDependency(tempDir, "", "hiltFeature", DiMode.HILT)

            Then("it should add metadata dependency to build file and catalog") {
                result shouldBe true
                val buildContent = buildFile.readText()
                buildContent shouldContain "ksp(libs.kotlin.metadata.jvm)"

                val catalogContent = catalogFile.readText()
                catalogContent shouldContain "kotlin-metadata-jvm = \"2.3.0\""
                catalogContent shouldContain "kotlin-metadata-jvm = { group = \"org.jetbrains.kotlin\", name = \"kotlin-metadata-jvm\", version.ref = \"kotlin-metadata-jvm\" }"
            }
        }

        When("ensureAppDependency is called with Hilt but catalog missing") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            if (catalogFile.exists()) catalogFile.toFile().delete()

            val result = repository.ensureAppDependency(tempDir, "", "hiltFeatureNoCatalog", DiMode.HILT)

            Then("it should not crash and return true (because build file updated)") {
                result shouldBe true
                val buildContent = buildFile.readText()
                buildContent shouldContain "ksp(libs.kotlin.metadata.jvm)"
            }
        }

        When("ensureAppDependency is called with Hilt and existing metadata dependency") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {\n    ksp(libs.kotlin.metadata.jvm)\n}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            catalogFile.writeText("[versions]\nkotlin-metadata-jvm = \"2.3.0\"\n[libraries]\nkotlin-metadata-jvm = { group = \"org.jetbrains.kotlin\", name = \"kotlin-metadata-jvm\", version.ref = \"kotlin-metadata-jvm\" }\n")

            val result = repository.ensureAppDependency(tempDir, "", "hiltFeatureExisting", DiMode.HILT)

            Then("it should return true (because feature dependency added) but not duplicate metadata") {
                result shouldBe true
                val buildContent = buildFile.readText()
                buildContent.lines().count { it.contains("ksp(libs.kotlin.metadata.jvm)") } shouldBe 1
                
                val catalogContent = catalogFile.readText()
                catalogContent.lines().count { it.contains("kotlin-metadata-jvm = \"2.3.0\"") } shouldBe 1
            }
        }

        When("ensureAppDependency is called with Hilt and missing sections in catalog") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            catalogFile.writeText("# Empty catalog")

            val result = repository.ensureAppDependency(tempDir, "", "hiltFeatureEmptyCatalog", DiMode.HILT)

            Then("it should add sections and metadata dependency") {
                result shouldBe true
                val catalogContent = catalogFile.readText()
                catalogContent shouldContain "[versions]"
                catalogContent shouldContain "kotlin-metadata-jvm = \"2.3.0\""
                catalogContent shouldContain "[libraries]"
                catalogContent shouldContain "kotlin-metadata-jvm = {"
            }
        }

        When("ensureAppDependency is called with Hilt and existing sections but missing metadata") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            catalogFile.writeText("[versions]\nother-lib = \"1.0\"\n[libraries]\nother-lib = { module = \"com.example:lib\", version.ref = \"other-lib\" }")

            val result = repository.ensureAppDependency(tempDir, "", "hiltFeatureExistingSections", DiMode.HILT)

            Then("it should add metadata dependency to existing sections") {
                result shouldBe true
                val catalogContent = catalogFile.readText()
                catalogContent shouldContain "[versions]"
                catalogContent shouldContain "kotlin-metadata-jvm = \"2.3.0\""
                catalogContent shouldContain "[libraries]"
                catalogContent shouldContain "kotlin-metadata-jvm = {"
                // Ensure existing content is preserved
                catalogContent shouldContain "other-lib = \"1.0\""
            }
        }

        When("ensureAppDependency is called with Koin") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")

            val result = repository.ensureAppDependency(tempDir, "", "koinFeature", DiMode.KOIN)

            Then("it should NOT add metadata dependency") {
                result shouldBe true
                val buildContent = buildFile.readText()
                buildContent shouldNotContain "ksp(libs.kotlin.metadata.jvm)"
            }
        }
    }
})
