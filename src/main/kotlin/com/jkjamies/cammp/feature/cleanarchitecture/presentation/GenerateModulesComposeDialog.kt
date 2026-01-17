package com.jkjamies.cammp.feature.cleanarchitecture.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.RadioButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenerateModulesScreen(
    state: GenerateModulesUiState,
    onIntent: (GenerateModulesIntent) -> Unit,
    onBrowseRoot: () -> Unit,
) {
    val rootState: TextFieldState = rememberTextFieldState()
    val featureState: TextFieldState = rememberTextFieldState()
    val orgCenterState: TextFieldState = rememberTextFieldState()

    LaunchedEffect(state.root) { rootState.setTextAndPlaceCursorAtEnd(state.root) }
    LaunchedEffect(state.feature) { featureState.setTextAndPlaceCursorAtEnd(state.feature) }
    LaunchedEffect(state.orgCenter) { orgCenterState.setTextAndPlaceCursorAtEnd(state.orgCenter) }

    LaunchedEffect(Unit) {
        snapshotFlow { rootState.text }.collect { onIntent(GenerateModulesIntent.SetRoot(it.toString())) }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { featureState.text }.collect { onIntent(GenerateModulesIntent.SetFeature(it.toString())) }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { orgCenterState.text }.collect { onIntent(GenerateModulesIntent.SetOrgCenter(it.toString())) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LabeledRow(label = "Root folder under project (e.g., features):") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(state = rootState, modifier = Modifier.weight(1f))
                DefaultButton(onClick = onBrowseRoot) { Text("Browse…") }
            }
        }
        LabeledRow(label = "Feature name (e.g., payments):") {
            TextField(state = featureState)
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LabeledRow(label = "Organization:") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("com.")
                    TextField(state = orgCenterState)
                    Text(state.orgRightPreview)
                }
            }
        }

        Text("Platform:")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioWithLabel(
                label = "Android",
                selected = state.platformAndroid,
                enabled = true,
                onClick = {
                    if (!state.platformAndroid) {
                        onIntent(GenerateModulesIntent.SetPlatformAndroid(true))
                    }
                }
            )
            RadioWithLabel(
                label = "Kotlin Multiplatform (KMP) (coming soon)",
                selected = state.platformKmp,
                enabled = false,
                onClick = {}
            )
        }

        Text("Data:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.includeDatasource,
                onCheckedChange = { onIntent(GenerateModulesIntent.SetIncludeDatasource(it)) })
            Text("Include datasource")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CheckboxWithLabel(
                label = "Remote",
                checked = state.datasourceRemote,
                enabled = state.includeDatasource && !state.datasourceCombined
            ) {
                onIntent(GenerateModulesIntent.SetDatasourceRemote(it))
            }
            CheckboxWithLabel(
                label = "Local",
                checked = state.datasourceLocal,
                enabled = state.includeDatasource && !state.datasourceCombined
            ) {
                onIntent(GenerateModulesIntent.SetDatasourceLocal(it))
            }
            CheckboxWithLabel(
                label = "Combined",
                checked = state.datasourceCombined,
                enabled = state.includeDatasource
            ) {
                onIntent(GenerateModulesIntent.SetDatasourceCombined(it))
            }
        }

        Text("Dependency Injection:")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RadioWithLabel(
                label = "Metro (coming soon)",
                selected = state.diMetro,
                enabled = false,
                onClick = {}
            )
            RadioWithLabel(label = "Hilt", selected = state.diHilt, enabled = !state.platformKmp) {
                onIntent(GenerateModulesIntent.SelectDiHilt(true))
            }
            RadioWithLabel(label = "Koin", selected = state.diKoin, enabled = true) {
                onIntent(GenerateModulesIntent.SelectDiKoin(true))
            }
            if (state.diKoin) {
                CheckboxWithLabel(label = "Koin Annotations", checked = state.diKoinAnnotations, enabled = true) {
                    onIntent(GenerateModulesIntent.SetKoinAnnotations(it))
                }
            }
        }
        if (state.diMetro || (state.diKoin && state.diKoinAnnotations)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.includeDiModule,
                    enabled = !state.diMetro,
                    onCheckedChange = { onIntent(GenerateModulesIntent.SetIncludeDiModule(it)) })
                Text("Include DI module")
            }
        }

        if (state.diKoin && state.diKoinAnnotations && !state.includeDiModule) {
            Text("Module scan will need to be manually set up.")
        }
        if (state.diMetro) {
            Text("Metro assumes AppScope and can be manually changed after generation.")
        }

        Text("Presentation:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.includePresentation,
                onCheckedChange = { onIntent(GenerateModulesIntent.SetIncludePresentation(it)) })
            Text("Include presentation module")
        }

        val isValid =
            state.projectBasePath?.isNotBlank() == true && state.root.isNotBlank() && state.feature.isNotBlank()
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DefaultButton(
                onClick = { onIntent(GenerateModulesIntent.Generate) },
                enabled = isValid && !state.isGenerating
            ) {
                Text(if (state.isGenerating) "Generating…" else "Generate")
            }
        }

        state.errorMessage?.let { Text("Error: $it") }
        state.lastMessage?.let { Text(it) }
        if (state.lastCreated.isNotEmpty()) {
            Text("Created: " + state.lastCreated.joinToString())
        }
        if (state.lastSkipped.isNotEmpty()) {
            Text("Skipped (already existed): " + state.lastSkipped.joinToString())
        }
        state.settingsUpdated?.let { Text("settings.gradle.kts updated: $it") }
        state.buildLogicCreated?.let { Text("build-logic scaffolded/updated: $it") }
    }
}

@Composable
private fun LabeledRow(label: String, content: @Composable RowScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label)
        Row(content = content)
    }
}

@Composable
fun CheckboxWithLabel(label: String, checked: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

@Composable
private fun RadioWithLabel(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, enabled = enabled, onClick = onClick)
        Text(label)
    }
}
