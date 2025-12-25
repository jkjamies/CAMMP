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

        CollapsibleSection("Recommended Workflow") {
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

        CollapsibleSection("Clean Architecture Generator") {
            Text("Generates a full feature module structure including data, domain, and presentation layers. It also generates convention plugins and updates your settings.gradle.kts and build-logic.")

            CollapsibleSubSection("What is Generated") {
                BulletedList(
                    "Feature module structure (data, domain, presentation)",
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

            CollapsibleSubSection("Convention Plugins & Version Catalog") {
                Text("This generator adds convention plugins to your project and updates the version catalog (libs.versions.toml) with the generated convention plugins. It assumes the existence of a version catalog.")
                Text("The following items are assumed to be present in your version catalog (they are NOT automatically added):")

                CollapsibleSubSection("Plugins") {
                    BulletedList(
                        "android-library",
                        "kotlin-android",
                        "ksp",
                        "parcelize",
                        "kotlin-serialization",
                        "compose-compiler",
                        "hilt (if Hilt selected)"
                    )
                }

                CollapsibleSubSection("Dependencies") {
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
                        "koin-ksp-compiler (if Koin Annotations selected)"
                    )
                }

                CollapsibleSubSection("Test Dependencies") {
                    BulletedList(
                        "kotest-runner",
                        "kotest-assertion",
                        "kotest-property",
                        "mockk",
                        "coroutines-test",
                        "turbine"
                    )
                }

                CollapsibleSubSection("UI Test Dependencies") {
                    BulletedList(
                        "androidx-test-runner",
                        "compose-ui-test",
                        "mockk-android",
                        "espresso",
                        "androidx-navigation-testing"
                    )
                }
            }

            CollapsibleSubSection("Dependency Injection") {
                Text("Supports Hilt and Koin. For Koin, you can choose between standard DSL and Koin Annotations.")
                Spacer(Modifier.height(4.dp))
                Text(
                    boldItem(
                        "Note: ",
                        "The generator adds the feature's DI module as a dependency to the app module.",
                        StringBuilder().apply {
                            append(" This transitively adds all feature dependencies (domain, data, presentation) to the app module's classpath. ")
                            append("This is standard for Hilt (to generate components) and Koin (to load modules), ")
                            append("but be aware that the app module will have visibility of the entire feature. ")
                            append("You can remove this if using Koin or modify it yourself if you need to fit for different architecture.")
                        }.toString()
                    ),
                    fontSize = 12.sp
                )
            }
        }

        CollapsibleSection("Repository Generator") {
            Text("Generates a Repository interface in the domain layer and an implementation in the data layer. Can also generate Datasources.")

            CollapsibleSubSection("What is Generated") {
                BulletedList(
                    "Repository Interface (in domain)",
                    "Repository Implementation (in data)",
                    "Datasource Interface (in data, if 'Include datasource' selected)",
                    "Datasource Implementation (in datasource/local/remote module, if 'Include datasource' selected)",
                    "DI Module update (Hilt or Koin based on selection)"
                )
            }

            CollapsibleSubSection("Assumptions") {
                BulletedList(
                    "You select a 'data' module directory.",
                    "Datasources are detected from the same module if available."
                )
            }
        }

        CollapsibleSection("Use Case Generator") {
            Text("Generates a UseCase class in the domain layer.")

            CollapsibleSubSection("What is Generated") {
                BulletedList(
                    "UseCase class (in domain)",
                    "DI Module update (Hilt or Koin based on selection)"
                )
            }

            CollapsibleSubSection("Assumptions") {
                BulletedList(
                    "You select a 'domain' module directory.",
                    "Repositories are detected from the 'repository' package within the domain module."
                )
            }
        }

        CollapsibleSection("Presentation Generator") {
            Text("Generates a Screen, ViewModel, and related components (State, Intent/Event). Supports MVI and MVVM patterns.")

            CollapsibleSubSection("What is Generated") {
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

            CollapsibleSubSection("Assumptions") {
                BulletedList(
                    "You select a 'presentation' module directory.",
                    "UseCases are detected from across the project to be injected into the ViewModel."
                )
            }

            CollapsibleSubSection("UI Patterns") {
                Text("Choose between MVI (Model-View-Intent) or MVVM (Model-View-ViewModel) architectures.")
            }
        }
    }
}

@Composable
internal fun CollapsibleSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
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
internal fun CollapsibleSubSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
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
