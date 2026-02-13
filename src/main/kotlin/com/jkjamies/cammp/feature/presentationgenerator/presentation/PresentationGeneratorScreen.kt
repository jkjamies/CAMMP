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

package com.jkjamies.cammp.feature.presentationgenerator.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

@Composable
fun PresentationGeneratorScreen(
    state: PresentationUiState,
    onIntent: (PresentationIntent) -> Unit,
    onBrowseDirectory: () -> Unit = {}
) {
    val dirState: TextFieldState = rememberTextFieldState()
    val nameState: TextFieldState = rememberTextFieldState()
    val pkgState: TextFieldState = rememberTextFieldState()

    LaunchedEffect(state.directory) { dirState.setTextAndPlaceCursorAtEnd(state.directory) }
    LaunchedEffect(state.screenName) { nameState.setTextAndPlaceCursorAtEnd(state.screenName) }
    LaunchedEffect(state.pkg) { pkgState.setTextAndPlaceCursorAtEnd(state.pkg) }

    LaunchedEffect(Unit) { snapshotFlow { dirState.text }.collect { onIntent(PresentationIntent.SetDirectory(it.toString())) } }
    LaunchedEffect(Unit) { snapshotFlow { nameState.text }.collect { onIntent(PresentationIntent.SetScreenName(it.toString())) } }
    LaunchedEffect(Unit) { snapshotFlow { pkgState.text }.collect { onIntent(PresentationIntent.SetPackage(it.toString())) } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Directory (desired presentation module):")
            TextField(state = dirState, modifier = Modifier.weight(1f))
            DefaultButton(onClick = onBrowseDirectory) { Text("Browse…") }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Screen name (e.g., HomeScreen):")
            Spacer(Modifier.width(8.dp))
            TextField(state = nameState)
        }
        CheckboxWithLabel(
            label = "Use Flow StateHolder",
            checked = state.useFlowStateHolder,
            enabled = true,
            onCheckedChange = { onIntent(PresentationIntent.ToggleFlowStateHolder(it)) }
        )
        CheckboxWithLabel(
            label = "Use Screen StateHolder",
            checked = state.useScreenStateHolder,
            enabled = true,
            onCheckedChange = { onIntent(PresentationIntent.ToggleScreenStateHolder(it)) }
        )
        CheckboxWithLabel(
            label = "Add Navigation Destination (Navigation Compose)",
            checked = state.includeNavigation,
            enabled = true,
            onCheckedChange = { onIntent(PresentationIntent.ToggleIncludeNavigation(it)) }
        )
        Text("UI Architecture Pattern:")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioWithLabel(
                label = "MVI",
                selected = state.patternMVI,
                enabled = true,
                onClick = { if (!state.patternMVI) onIntent(PresentationIntent.SetPatternMVI(true)) }
            )
            RadioWithLabel(
                label = "MVVM",
                selected = state.patternMVVM,
                enabled = true,
                onClick = { if (!state.patternMVVM) onIntent(PresentationIntent.SetPatternMVVM(true)) }
            )
            RadioWithLabel(
                label = "Circuit (coming soon)",
                selected = state.patternCircuit,
                enabled = false,
                onClick = {}
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("Domain UseCases (across project):")
        if (state.useCasesByModule.isEmpty()) {
            Text("No domain UseCases found across project")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val sorted = state.useCasesByModule.toSortedMap()
                sorted.forEach { (modulePath, fqnList) ->
                    item { Text("$modulePath:") }
                    items(fqnList.sorted()) { fqn ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = state.selectedUseCases.contains(fqn), onCheckedChange = { sel ->
                                onIntent(PresentationIntent.ToggleUseCaseSelection(fqn, sel))
                            })
                            Text(fqn)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Dependency Injection:")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioWithLabel(
                label = "Metro",
                selected = state.diMetro,
                enabled = true,
                onClick = { if (!state.diMetro) onIntent(PresentationIntent.SetDiMetro(true)) }
            )
            RadioWithLabel(
                label = "Hilt",
                selected = state.diHilt,
                enabled = true,
                onClick = { if (!state.diHilt) onIntent(PresentationIntent.SetDiHilt(true)) }
            )
            RadioWithLabel(
                label = "Koin",
                selected = state.diKoin,
                enabled = true,
                onClick = { if (!state.diKoin) onIntent(PresentationIntent.SetDiKoin(true)) }
            )
            if (state.diKoin) {
                CheckboxWithLabel(
                    label = "Koin Annotations",
                    checked = state.diKoinAnnotations,
                    enabled = true,
                    onCheckedChange = { onIntent(PresentationIntent.ToggleKoinAnnotations(it)) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        val isPresentationModule = state.directory.trimEnd('/', '\\').substringAfterLast('/').substringAfterLast('\\')
            .contains("presentation", ignoreCase = true)
        val isValid = state.directory.isNotBlank() && state.screenName.isNotBlank() && isPresentationModule
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DefaultButton(
                onClick = { onIntent(PresentationIntent.Generate) },
                enabled = isValid && !state.isGenerating
            ) {
                Text(if (state.isGenerating) "Generating…" else "Generate")
            }
        }

        val inlineError =
            if (!isPresentationModule && state.directory.isNotBlank()) "Selected directory must be a presentation module" else null
        val errorText = state.errorMessage ?: inlineError
        if (errorText != null) {
            Text("Error: $errorText", color = Color(0xFFD32F2F))
        }
        state.lastMessage?.let { msg ->
            val lines = msg.lineSequence().toList()
            if (lines.isNotEmpty()) {
                Text(lines.first())
                lines.drop(1).forEach { Text(it) }
            }
        }
    }
}

@Composable
private fun RadioWithLabel(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        org.jetbrains.jewel.ui.component.RadioButton(selected = selected, enabled = enabled, onClick = onClick)
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
