package com.github.jkjamies.cammp.feature.repositorygenerator.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.RadioButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

@Composable
fun RepositoryGeneratorScreen(state: RepositoryUiState, onIntent: (RepositoryIntent) -> Unit, onBrowseDataDir: () -> Unit = {}) {
    val dirState: TextFieldState = rememberTextFieldState()
    val nameState: TextFieldState = rememberTextFieldState()

    LaunchedEffect(state.domainPackage) { dirState.setTextAndPlaceCursorAtEnd(state.domainPackage) }
    LaunchedEffect(state.name) { nameState.setTextAndPlaceCursorAtEnd(state.name) }

    LaunchedEffect(Unit) { snapshotFlow { dirState.text }.collect { onIntent(RepositoryIntent.SetDomainPackage(it.toString())) } }
    LaunchedEffect(Unit) { snapshotFlow { nameState.text }.collect { onIntent(RepositoryIntent.SetName(it.toString())) } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Data module directory:")
            TextField(state = dirState, modifier = Modifier.weight(1f))
            DefaultButton(onClick = onBrowseDataDir) { Text("Browse…") }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Repository name:")
            Spacer(Modifier.width(8.dp))
            TextField(state = nameState)
        }
        Text("Data source(s):")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = state.includeDatasource, onCheckedChange = { onIntent(RepositoryIntent.SetIncludeDatasource(it)) })
            Text("Include datasource")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CheckboxWithLabel(
                label = "Remote",
                checked = state.datasourceRemote,
                enabled = state.includeDatasource && !state.datasourceCombined,
                onCheckedChange = { onIntent(RepositoryIntent.SetDatasourceRemote(it)) }
            )
            CheckboxWithLabel(
                label = "Local",
                checked = state.datasourceLocal,
                enabled = state.includeDatasource && !state.datasourceCombined,
                onCheckedChange = { onIntent(RepositoryIntent.SetDatasourceLocal(it)) }
            )
            CheckboxWithLabel(
                label = "Combined",
                checked = state.datasourceCombined,
                enabled = state.includeDatasource,
                onCheckedChange = { onIntent(RepositoryIntent.SetDatasourceCombined(it)) }
            )
        }
        val dsMap = state.dataSourcesByType
        Spacer(Modifier.height(4.dp))
        Text("Available Datasources (scoped to this feature):")
        if (dsMap.isEmpty()) {
            Text("No datasources found in this feature’s data module.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val sorted = dsMap.toSortedMap()
                sorted.forEach { (type, list) ->
                    item { Text(type + ":") }
                    items(list) { fqn ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = state.selectedDataSources.contains(fqn),
                                onCheckedChange = { sel -> onIntent(RepositoryIntent.ToggleDataSourceSelection(fqn, sel)) }
                            )
                            Text(fqn)
                        }
                    }
                }
            }
        }
        Text("Dependency Injection:")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioWithLabel(label = "Hilt", selected = state.diHilt, enabled = true) {
                if (!state.diHilt) onIntent(RepositoryIntent.SetDiHilt(true))
            }
            RadioWithLabel(label = "Koin", selected = state.diKoin, enabled = true) {
                if (!state.diKoin) onIntent(RepositoryIntent.SetDiKoin(true))
            }
            if (state.diKoin) {
                CheckboxWithLabel(label = "Koin Annotations", checked = state.diKoinAnnotations, enabled = true, onCheckedChange = { onIntent(RepositoryIntent.ToggleKoinAnnotations(it)) })
            }
        }
        val isDataModule = state.domainPackage.trimEnd('/', '\\').substringAfterLast('/').substringAfterLast('\\').equals("data", ignoreCase = true)
        val isValid = state.name.isNotBlank() && state.domainPackage.isNotBlank() && isDataModule
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DefaultButton(onClick = { onIntent(RepositoryIntent.Generate) }, enabled = isValid && !state.isGenerating) {
                Text(if (state.isGenerating) "Generating…" else "Generate")
            }
        }
        val inlineError = if (!isDataModule && state.domainPackage.isNotBlank()) "Selected directory must be a data module" else null
        val errorText = state.errorMessage ?: inlineError
        if (errorText != null) {
            Text("Error: $errorText", color = Color(0xFFD32F2F))
        }
        if (state.errorMessage == null && state.lastGeneratedMessage != null) {
            val lines = state.lastGeneratedMessage.lineSequence().toList()
            if (lines.isNotEmpty()) {
                Text(lines.first())
                lines.drop(1).forEach { Text(it) }
            }
        }
    }
}

@Composable
private fun RadioWithLabel(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, enabled = enabled, onClick = onClick)
        Text(label)
    }
}

@Composable
private fun CheckboxWithLabel(label: String, checked: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
