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

            And("catalog file does not exist and gradle dir does not exist") {
                if (gradleDir.exists()) gradleDir.toFile().deleteRecursively()
                Then("it should create gradle dir and catalog file with aliases") {
                    val result = repository.ensureVersionCatalogPluginAliases(tempDir, "myorg", listOf("domain", "data"))
                    result shouldBe true
                    gradleDir.exists() shouldBe true
                    catalogFile.exists() shouldBe true
                    val content = catalogFile.readText()
                    content shouldContain "convention-android-library-domain = { id = \"com.myorg.convention.android.library.domain\" }"
                }
            }

            And("orgSegment is empty") {
                if (catalogFile.exists()) catalogFile.toFile().delete()
                Then("it should use default 'cammp' org") {
                    val result = repository.ensureVersionCatalogPluginAliases(tempDir, " ", listOf("domain"))
                    result shouldBe true
                    val content = catalogFile.readText()
                    content shouldContain "id = \"com.cammp.convention.android.library.domain\""
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

            And("org name has uppercase letters") {
                Then("it should lowercase the first letter of org name in plugin ID") {
                    val result = repository.ensureVersionCatalogPluginAliases(tempDir, "MyOrg", listOf("di"))
                    result shouldBe true
                    val content = catalogFile.readText()
                    // Should be com.myOrg... (first letter lowercased)
                    content shouldContain "convention-android-library-di = { id = \"com.myOrg.convention.android.library.di\" }"
                }
            }

            And("catalog file exists but missing plugins section") {
                catalogFile.writeText("[versions]\n")
                Then("it should add plugins section and aliases") {
                    val result = repository.ensureVersionCatalogPluginAliases(tempDir, "myorg", listOf("domain"))
                    result shouldBe true
                    val content = catalogFile.readText()
                    content shouldContain "[plugins]"
                    content shouldContain "convention-android-library-domain"
                }
            }

            And("catalog file exists but missing plugins section and no newline at end") {
                catalogFile.writeText("[versions]")
                Then("it should add newline and plugins section") {
                    val result = repository.ensureVersionCatalogPluginAliases(tempDir, "myorg", listOf("domain"))
                    result shouldBe true
                    val content = catalogFile.readText()
                    content shouldContain "[versions]\n\n[plugins]"
                    content shouldContain "convention-android-library-domain"
                }
            }

            And("data source modules are enabled (combined or split)") {
                Then("it should add aliases for the enabled data source types") {
                    // Test combined dataSource
                    val result1 = repository.ensureVersionCatalogPluginAliases(tempDir, "myorg", listOf("dataSource"))
                    result1 shouldBe true
                    var content = catalogFile.readText()
                    content shouldContain "convention-android-library-dataSource"
                    content shouldNotContain "convention-android-library-remoteDataSource"
                    content shouldNotContain "convention-android-library-localDataSource"

                    // Test split data sources (simulating a different run or feature)
                    // Note: In a real scenario, we wouldn't remove the previous alias if we are just appending, 
                    // but here we are testing that the *new* aliases are added.
                    val result2 = repository.ensureVersionCatalogPluginAliases(tempDir, "myorg", listOf("remoteDataSource", "localDataSource"))
                    result2 shouldBe true
                    content = catalogFile.readText()
                    content shouldContain "convention-android-library-remoteDataSource"
                    content shouldContain "convention-android-library-localDataSource"
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

        When("ensureAppDependency is called with Hilt and Kotlin 2.3.0") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            catalogFile.writeText("[versions]\nkotlin = \"2.3.0\"\n[libraries]\n")

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

        When("ensureAppDependency is called with Hilt and Kotlin 2.0.21") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            catalogFile.writeText("[versions]\nkotlin = \"2.0.21\"\n[libraries]\n")

            val result = repository.ensureAppDependency(tempDir, "", "hiltFeatureOldKotlin", DiMode.HILT)

            Then("it should NOT add metadata dependency") {
                result shouldBe true // Still true because feature dependency is added
                val buildContent = buildFile.readText()
                buildContent shouldNotContain "ksp(libs.kotlin.metadata.jvm)"

                val catalogContent = catalogFile.readText()
                catalogContent shouldNotContain "kotlin-metadata-jvm"
            }
        }

        When("ensureAppDependency is called with Hilt and Kotlin version is missing from catalog") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            catalogFile.writeText("[versions]\n[libraries]\n") // No kotlin version

            val result = repository.ensureAppDependency(tempDir, "", "hiltFeatureNoKotlinVersion", DiMode.HILT)

            Then("it should NOT add metadata dependency") {
                result shouldBe true // Still true because feature dependency is added
                val buildContent = buildFile.readText()
                buildContent shouldNotContain "ksp(libs.kotlin.metadata.jvm)"
            }
        }

        When("ensureAppDependency is called with Hilt and Kotlin 2.4.0 (newer)") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            catalogFile.writeText("[versions]\nkotlin = \"2.4.0\"\n[libraries]\n")

            val result = repository.ensureAppDependency(tempDir, "", "hiltFeatureNewerKotlin", DiMode.HILT)

            Then("it should add metadata dependency") {
                result shouldBe true
                val buildContent = buildFile.readText()
                buildContent shouldContain "ksp(libs.kotlin.metadata.jvm)"
            }
        }

        When("ensureAppDependency is called with Hilt and Kotlin 2.3.0.1 (patch newer)") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            catalogFile.writeText("[versions]\nkotlin = \"2.3.0.1\"\n[libraries]\n")

            val result = repository.ensureAppDependency(tempDir, "", "hiltFeaturePatchNewer", DiMode.HILT)

            Then("it should add metadata dependency") {
                result shouldBe true
                val buildContent = buildFile.readText()
                buildContent shouldContain "ksp(libs.kotlin.metadata.jvm)"
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

            Then("it should not crash and return true (because build file updated) but NOT add metadata dependency (cant check version)") {
                result shouldBe true
                val buildContent = buildFile.readText()
                buildContent shouldNotContain "ksp(libs.kotlin.metadata.jvm)"
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
            catalogFile.writeText("[versions]\nkotlin = \"2.3.0\"\nkotlin-metadata-jvm = \"2.3.0\"\n[libraries]\nkotlin-metadata-jvm = { group = \"org.jetbrains.kotlin\", name = \"kotlin-metadata-jvm\", version.ref = \"kotlin-metadata-jvm\" }\n")

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
            catalogFile.writeText("[versions]\nkotlin = \"2.3.0\"\n# Empty catalog")

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

        When("ensureAppDependency is called with Hilt and existing libraries section but missing metadata") {
            val appDir = tempDir.resolve("app")
            appDir.createDirectories()
            val buildFile = appDir.resolve("build.gradle.kts")
            buildFile.writeText("dependencies {}")
            val gradleDir = tempDir.resolve("gradle")
            gradleDir.createDirectories()
            val catalogFile = gradleDir.resolve("libs.versions.toml")
            catalogFile.writeText("[versions]\nkotlin = \"2.3.0\"\n[libraries]\nother-lib = { module = \"com.example:lib\" }")

            val result = repository.ensureAppDependency(tempDir, "", "hiltFeatureExistingLibrary", DiMode.HILT)

            Then("it should add metadata dependency to existing libraries section") {
                result shouldBe true
                val catalogContent = catalogFile.readText()
                catalogContent shouldContain "[libraries]"
                catalogContent shouldContain "kotlin-metadata-jvm = {"
                catalogContent shouldContain "other-lib = { module = \"com.example:lib\" }"
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
