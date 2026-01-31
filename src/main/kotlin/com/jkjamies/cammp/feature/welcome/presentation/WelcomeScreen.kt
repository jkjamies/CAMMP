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

package com.jkjamies.cammp.feature.welcome.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
internal fun WelcomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Welcome to CAMMP", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Clean Architecture Multiplatform Modular Plugin", fontSize = 14.sp)

        Text(
            "This plugin helps you generate Clean Architecture components for your Android and Multiplatform projects. Below is a guide on how to use the available generators.",
            fontSize = 14.sp
        )

        CollapsibleSection("Recommended Workflow", "RecommendedWorkflow") {
            Text("To get the most out of CAMMP, recommended is the following workflow:")
            Spacer(Modifier.height(8.dp))
            BulletedList(
                boldItem(
                    "Use the ",
                    "Clean Architecture Generator",
                    " to create your feature module structure and setup convention plugins."
                ),
                boldItem(
                    "Use the ",
                    "Repository Generator",
                    " to create your repositories and datasources. Subsequent repositories can select existing datasources within the feature."
                ),
                boldItem(
                    "Use the ",
                    "Use Case Generator",
                    " to create domain logic. Existing repositories within the feature will be available for selection."
                ),
                boldItem(
                    "Use the ",
                    "Presentation Generator",
                    " to create your UI. Existing UseCases across the project will be available for selection in the ViewModel."
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                boldItem(
                    "",
                    "Note: ",
                    "Files generated are encouraged to be modified, it is not a one size fits all and solutions should be modified to fit specific usecases of the project - it is merely a starting point and tool for consistency - more options and feedback are welcome as they fit generic/selectable usecases."
                ),
                fontSize = 14.sp
            )
        }

        CollapsibleSection("Clean Architecture Generator", "CleanArchitectureGenerator") {
            Text("Generates a full feature module structure including data, domain, and presentation layers. It also generates convention plugins and updates your settings.gradle.kts and build-logic.")

            CollapsibleSubSection("What is Generated", "CleanArch:WhatIsGenerated") {
                BulletedList(
                    "Feature module structure (data, domain, presentation)",
                    "Optional 'api' module for Use Case interfaces",
                    "Convention plugins (in build-logic)",
                    "Updates to settings.gradle.kts",
                    "Updates to libs.versions.toml",
                    "Datasource interface and implementation (if 'Include datasource' selected)",
                    "Local Datasource (if 'Local' selected)",
                    "Remote Datasource (if 'Remote' selected)",
                    "Combined Datasource (if 'Combined' selected)",
                    "DI Module (Hilt or Koin based on selection)"
                )
            }

            CollapsibleSubSection("Convention Plugins & Version Catalog", "CleanArch:ConventionPlugins") {
                Text("This generator adds convention plugins to your project and updates the version catalog (libs.versions.toml) with the generated convention plugins. It assumes the existence of a version catalog.")
                Spacer(Modifier.height(8.dp))
                Text(
                    boldItem(
                        "Note: ",
                        "AGP Version: ",
                        "Assumes AGP 8.13.2 or earlier. AGP 9+ may require manual modifications to the generated convention plugins."
                    ),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    boldItem(
                        "Note: ",
                        "Kotlin/JDK Version: ",
                        "Assumes Kotlin and JDK 17. Please ensure your project is configured with the Kotlin JVM toolchain set to 17 in your build files (e.g., kotlin { jvmToolchain(17) })."
                    ),
                    fontSize = 12.sp
                )

                CollapsibleSubSection("Version Catalog Updates", "CleanArch:VersionCatalogUpdates") {
                    Text("Dependencies in the libs.versions.toml file will be used if they exist. If they do not exist, they will be added automatically.")
                }

                CollapsibleSubSection("Plugins", "CleanArch:Plugins") {
                    BulletedList(
                        "android-library",
                        "kotlin-android",
                        "ksp",
                        "parcelize",
                        "kotlin-serialization",
                        "compose-compiler",
                        "hilt (if Hilt selected)",
                        "metro (if Metro selected - coming soon)"
                    )
                }

                CollapsibleSubSection("Dependencies", "CleanArch:Dependencies") {
                    BulletedList(
                        "kotlinx-serialization",
                        "json",
                        "androidx-core-ktx",
                        "compose-ui",
                        "compose-material3-android",
                        "compose-navigation",
                        "compose-tooling",
                        "compose-preview",
                        "coroutines-core",
                        "coroutines-android",
                        "hilt (if Hilt selected)",
                        "hilt-compiler (if Hilt selected)",
                        "compose-hilt-navigation (if Hilt selected)",
                        "koin (if Koin selected)",
                        "koin-core (if Koin Annotations selected)",
                        "koin-annotations (if Koin Annotations selected)",
                        "koin-ksp-compiler (if Koin Annotations selected)",
                        "metro (if Metro selected - coming soon)"
                    )
                }

                CollapsibleSubSection("Test Dependencies", "CleanArch:TestDependencies") {
                    BulletedList(
                        "kotest-runner",
                        "kotest-assertion",
                        "kotest-property",
                        "mockk",
                        "coroutines-test",
                        "turbine"
                    )
                }

                CollapsibleSubSection("UI Test Dependencies", "CleanArch:UITestDependencies") {
                    BulletedList(
                        "androidx-test-runner",
                        "compose-ui-test",
                        "mockk-android",
                        "espresso",
                        "androidx-navigation-testing"
                    )
                }
                
                CollapsibleSubSection("Root build.gradle.kts Configuration", "CleanArch:RootConfig") {
                    Text("Please ensure the following plugins are added to your root build.gradle.kts if not already present:")
                    Spacer(Modifier.height(4.dp))
                    Text("plugins {", fontWeight = FontWeight.Bold)
                    Text("    alias(libs.plugins.android.application) apply false", fontWeight = FontWeight.Bold)
                    Text("    alias(libs.plugins.kotlin.compose) apply false", fontWeight = FontWeight.Bold)
                    Text("    alias(libs.plugins.hilt) apply false // if using Hilt", fontWeight = FontWeight.Bold)
                    Text("    alias(libs.plugins.kotlin.serialization) apply false", fontWeight = FontWeight.Bold)
                    Text("    alias(libs.plugins.ksp) apply false", fontWeight = FontWeight.Bold)
                    Text("    alias(libs.plugins.kotlin.android) apply false", fontWeight = FontWeight.Bold)
                    Text("}", fontWeight = FontWeight.Bold)
                }
            }

            CollapsibleSubSection("Dependency Injection", "CleanArch:DI") {
                Text("Supports Hilt, Koin, and Metro (coming soon). For Koin, you can choose between standard DSL and Koin Annotations.")
                Spacer(Modifier.height(4.dp))
                Text(
                    boldItem(
                        "Note: ",
                        "DI Module: ",
                        StringBuilder().apply {
                            append("A dedicated DI module is optional for Metro and Koin with Annotations. ")
                            append("If excluded for Koin w/Annotations, a manual module scan setup is required. ")
                            append("Metro currently assumes AppScope. ")
                            append("Standard Hilt and Koin DSL always include a DI module.")
                        }.toString()
                    ),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    boldItem(
                        "Note: ",
                        "Transitive Dependencies: ",
                        StringBuilder().apply {
                            append("The generator adds the feature's DI module as a dependency to the app module. ")
                            append("This transitively adds all feature dependencies (domain, data, presentation) to the app module's classpath. ")
                            append("This is standard for Hilt (to generate components) and Koin (to load modules), ")
                            append("but be aware that the app module will have visibility of the entire feature. ")
                            append("You can remove this if using Koin or modify it yourself if you need to fit for different architecture.")
                        }.toString()
                    ),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    boldItem(
                        "Note: ",
                        "If using Hilt and Kotlin 2.3.0+, ",
                        "the generator will automatically add the 'kotlin-metadata-jvm' dependency to your app module to resolve compatibility issues."
                    ),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    boldItem(
                        "Hilt Setup: ",
                        "Please ensure you have the standard Hilt setup in your project, including:",
                        ""
                    ),
                    fontSize = 12.sp
                )
                BulletedList(
                    "Hilt plugin in the base gradle file",
                    "Hilt plugin and dependency in the app module",
                    "KSP plugin and Hilt compiler dependency in the app module",
                    "An Application class with @HiltAndroidApp",
                    "Your MainActivity annotated with @AndroidEntryPoint"
                )
            }
        }

        CollapsibleSection("Repository Generator", "RepositoryGenerator") {
            Text("Generates a Repository interface in the domain layer and an implementation in the data layer. Can also generate Datasources.")

            CollapsibleSubSection("What is Generated", "Repo:WhatIsGenerated") {
                BulletedList(
                    "Repository Interface (in domain)",
                    "Repository Implementation (in data)",
                    "Datasource Interface (in data, if 'Include datasource' selected)",
                    "Datasource Implementation (in datasource/local/remote module, if 'Include datasource' selected)",
                    "DI Module update (Hilt or Koin based on selection)"
                )
            }

            CollapsibleSubSection("Assumptions", "Repo:Assumptions") {
                BulletedList(
                    "You select a 'data' module directory.",
                    "Datasources are detected from the same module if available."
                )
            }
        }

        CollapsibleSection("Use Case Generator", "UseCaseGenerator") {
            Text("Generates a UseCase class in the domain layer. If an 'api' module exists within the feature, it will also generate a UseCase interface in the 'api' module and make the implementation implement it.")

            CollapsibleSubSection("What is Generated", "UseCase:WhatIsGenerated") {
                BulletedList(
                    "UseCase class (in domain)",
                    "UseCase interface (in 'api' module, if it exists)",
                    "DI Module update (Hilt or Koin based on selection)"
                )
            }

            CollapsibleSubSection("Assumptions", "UseCase:Assumptions") {
                BulletedList(
                    "You select a 'domain' module directory.",
                    "Repositories are detected from the 'repository' package within the domain module."
                )
            }
        }

        CollapsibleSection("Presentation Generator", "PresentationGenerator") {
            Text("Generates a Screen, ViewModel, and related components (State, Intent/Event). Supports MVI and MVVM patterns.")

            CollapsibleSubSection("What is Generated", "Presentation:WhatIsGenerated") {
                BulletedList(
                    "Screen Composable",
                    "ViewModel",
                    "UiState",
                    "Intent/Event (if MVI selected)",
                    "Navigation Host (if 'Add Navigation Destination' selected)",
                    "Navigation Destination (if 'Add Navigation Destination' selected)",
                    "Screen State Holder (if 'Use Screen StateHolder' selected)",
                    "Flow State Holder (if 'Use Flow StateHolder' selected)",
                    "DI Module update (Hilt or Koin based on selection)"
                )
            }

            CollapsibleSubSection("Assumptions", "Presentation:Assumptions") {
                BulletedList(
                    "You select a 'presentation' module directory.",
                    "UseCases are detected from across the project to be injected into the ViewModel.",
                    "If a feature has both 'api' and 'domain' modules, UseCases from the 'api' module are preferred and the 'domain' implementations are excluded from the discovery results."
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    boldItem(
                        "Note: ",
                        "Navigation: ",
                        "The generator does not automatically handle navigation between features. You will need to manually import the generated Screen or NavHost and integrate it into your app's navigation graph, or set up deeplinks/activities/fragments as needed."
                    ),
                    fontSize = 12.sp
                )
            }

            CollapsibleSubSection("UI Patterns", "Presentation:UIPatterns") {
                Text("Choose between MVI (Model-View-Intent), MVVM (Model-View-ViewModel), or Circuit (coming soon) architectures.")
            }
        }
    }
}

@Composable
internal fun CollapsibleSection(title: String, testTag: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                key = if (expanded) AllIconsKeys.General.ArrowDown else AllIconsKeys.General.ArrowRight,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
            Spacer(Modifier.width(8.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}

@Composable
internal fun CollapsibleSubSection(title: String, testTag: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                key = if (expanded) AllIconsKeys.General.ArrowDown else AllIconsKeys.General.ArrowRight,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 20.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content
            )
        }
    }
}

@Composable
internal fun BulletedList(vararg items: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { item ->
            Row {
                Text("• ", fontWeight = FontWeight.Bold)
                Text(item)
            }
        }
    }
}

@Composable
internal fun BulletedList(vararg items: AnnotatedString) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { item ->
            Row {
                Text("• ", fontWeight = FontWeight.Bold)
                Text(item)
            }
        }
    }
}

private fun boldItem(prefix: String, boldPart: String, suffix: String): AnnotatedString {
    return buildAnnotatedString {
        append(prefix)
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(boldPart)
        }
        append(suffix)
    }
}
