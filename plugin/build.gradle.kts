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

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
    alias(libs.plugins.metro)
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
            )
        )
    }
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
    google()
}

dependencies {
    implementation(project(":core"))

    testImplementation(testFixtures(project(":core")))
    testImplementation(libs.junit)
    testImplementation(libs.opentest4j)
    testImplementation(libs.hamcrest)
    testImplementation(libs.composeuitest)
    testImplementation(libs.jewelstandalone)
    testImplementation(libs.skikoAwtRuntimeAll)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)

    testRuntimeOnly(libs.junit.vintage.engine)

    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        bundledModules(
            "intellij.libraries.skiko",
            "intellij.libraries.compose.foundation.desktop",
            "intellij.platform.jewel.foundation",
            "intellij.platform.jewel.ui",
            "intellij.platform.jewel.ideLafBridge",
            "intellij.platform.compose"
        )

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        description = providers.fileContents(layout.projectDirectory.file("../README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels = providers.gradleProperty("pluginVersion")
            .map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
    path.set(rootProject.layout.projectDirectory.file("CHANGELOG.md").asFile.absolutePath)
}

kover {
    reports {
        total {
            xml {
                onCheck = true
            }
            filters {
                excludes {
                    packages("*.di", "*.di.*")

                    classes(
                        "*MetroFactory*",
                        "*MetroContributionToAppScope*",
                        "*BindsMirror*",
                        "*ComposableSingletons*",
                    )

                    classes(
                        "com.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesComposeDialogKt",
                        "com.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesComposeDialog*",
                        "com.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryGeneratorScreenKt",
                        "com.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryGeneratorScreen*",
                        "com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseGeneratorScreenKt",
                        "com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseGeneratorScreen*",
                        "com.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationGeneratorScreenKt",
                        "com.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationGeneratorScreen*",
                    )

                    classes(
                        "com.jkjamies.cammp.feature.repositorygenerator.datasource.PackageMetadataDataSourceImpl",
                        "com.jkjamies.cammp.feature.usecasegenerator.datasource.PackageMetadataDataSourceImpl",
                        "com.jkjamies.cammp.feature.presentationgenerator.datasource.PackageMetadataDataSourceImpl",
                    )
                }
            }
        }
    }
}

tasks {
    publishPlugin {
        dependsOn(patchChangelog)
    }

    test {
        useJUnitPlatform()
        systemProperty("kotest.framework.config.fqn", "com.jkjamies.cammp.ProjectConfig")
    }
}

tasks.getByName("verifyPlugin").enabled = false
