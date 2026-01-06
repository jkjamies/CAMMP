package com.jkjamies.cammp.feature.cleanarchitecture.datasource

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Tests for [GradleSettingsDataSourceImpl].
 */
class GradleSettingsDataSourceImplTest : BehaviorSpec({

    Given("GradleSettingsDataSourceImpl") {
        val ds = GradleSettingsDataSourceImpl()

        When("ensureIncludes is run twice") {
            withTempDir("cammp_cleanarchitecture_gradle_ds") { projectBase ->
                projectBase.resolve("gradle").createDirectories()
                projectBase.resolve("settings.gradle.kts").writeText("// settings\n")

                val changed1 = ds.ensureIncludes(
                    projectBase = projectBase,
                    root = "feature",
                    feature = "profile",
                    modules = listOf("domain", "data"),
                )
                val changed2 = ds.ensureIncludes(
                    projectBase = projectBase,
                    root = "feature",
                    feature = "profile",
                    modules = listOf("domain", "data"),
                )

                changed1 to changed2
            }.let { (changed1, changed2) ->
                Then("first run changes, second run does not") {
                    changed1 shouldBe true
                    changed2 shouldBe false
                }
            }
            // validate contents
            withTempDir("cammp_cleanarchitecture_gradle_ds_contents") { projectBase ->
                projectBase.resolve("gradle").createDirectories()
                projectBase.resolve("settings.gradle.kts").writeText("// settings\n")

                ds.ensureIncludes(projectBase, "feature", "profile", listOf("domain", "data"))
                projectBase.resolve("settings.gradle.kts").readText()
            }.let { text ->
                Then("it should contain include lines") {
                    text.shouldContain("include(\":feature:profile:domain\")")
                    text.shouldContain("include(\":feature:profile:data\")")
                }
            }
        }

        When("ensureAppDependency is run") {
            val (changed, appText, catalogText) = withTempDir("cammp_cleanarchitecture_app_dep") { projectBase ->
                projectBase.resolve("gradle").createDirectories()
                projectBase.resolve("app").createDirectories()

                projectBase.resolve("app/build.gradle.kts").writeText(
                    """
                    plugins { }
                    dependencies {
                    }
                    """.trimIndent()
                )

                // Kotlin 2.3.0 which triggers metadata workaround for Hilt
                projectBase.resolve("gradle/libs.versions.toml").writeText(
                    """
                    [versions]
                    kotlin = "2.3.0"

                    [libraries]

                    [plugins]
                    kotlinMetadata = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
                    """.trimIndent()
                )

                val changed = ds.ensureAppDependency(
                    projectBase = projectBase,
                    root = "feature",
                    feature = "profile",
                    diMode = DiMode.HILT,
                )

                val appText = projectBase.resolve("app/build.gradle.kts").readText()
                val catalogText = projectBase.resolve("gradle/libs.versions.toml").readText()

                Triple(changed, appText, catalogText)
            }

            Then("it should report changed") {
                changed shouldBe true
            }

            Then("it should add the DI project dependency") {
                appText.shouldContain("implementation(project(\":feature:profile:di\"))")
            }

            Then("it should add the kotlin metadata dependency") {
                appText shouldContain "ksp(libs.kotlin.metadata.jvm)"
            }

            Then("it should add kotlin-metadata-jvm in the version catalog") {
                catalogText shouldContain "kotlin-metadata-jvm = \"2.3.0\""
                catalogText shouldContain "kotlin-metadata-jvm = { group = \"org.jetbrains.kotlin\", name = \"kotlin-metadata-jvm\", version.ref = \"kotlin-metadata-jvm\" }"
            }
        }

        When("ensureVersionCatalogPluginAliases is run") {
            Then("it should create libs.versions.toml when missing") {
                val (changed, catalogText) = withTempDir("cammp_versions_catalog_create") { projectBase ->
                    projectBase.resolve("gradle").createDirectories()

                    val changed = ds.ensureVersionCatalogPluginAliases(
                        projectBase = projectBase,
                        orgSegment = "Example",
                        enabledModules = listOf("domain", "data", "di", "presentation"),
                    )

                    val catalogText = projectBase.resolve("gradle/libs.versions.toml").readText()
                    changed to catalogText
                }

                changed shouldBe true
                catalogText shouldContain "[plugins]"
                catalogText shouldContain "convention-android-library-domain"
                catalogText shouldContain "com.example.convention.android.library.domain"
            }

            Then("it should add the [plugins] section if missing") {
                val (changed, text) = withTempDir("cammp_versions_catalog_add_plugins") { projectBase ->
                    projectBase.resolve("gradle").createDirectories()
                    projectBase.resolve("gradle/libs.versions.toml").writeText(
                        """
                        [versions]
                        kotlin = "2.2.0"

                        [libraries]
                        """.trimIndent()
                    )

                    val changed = ds.ensureVersionCatalogPluginAliases(
                        projectBase = projectBase,
                        orgSegment = "com.example",
                        enabledModules = listOf("domain", "data"),
                    )

                    changed to projectBase.resolve("gradle/libs.versions.toml").readText()
                }

                changed shouldBe true
                text shouldContain "[plugins]"
                text shouldContain "convention-android-library-domain"
                text shouldContain "convention-android-library-data"
            }

            Then("it should be idempotent when required aliases already exist") {
                val changed = withTempDir("cammp_versions_catalog_idempotent") { projectBase ->
                    projectBase.resolve("gradle").createDirectories()
                    projectBase.resolve("gradle/libs.versions.toml").writeText(
                        """
                        [versions]

                        [libraries]

                        [plugins]
                        convention-android-library-domain = { id = "com.example.convention.android.library.domain" }
                        convention-android-library-data = { id = "com.example.convention.android.library.data" }
                        """.trimIndent()
                    )

                    ds.ensureVersionCatalogPluginAliases(
                        projectBase = projectBase,
                        orgSegment = "com.example",
                        enabledModules = listOf("domain", "data"),
                    )
                }

                changed shouldBe false
            }
        }
    }
})
