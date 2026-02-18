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

package com.jkjamies.cammp.feature.usecasegenerator.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun UseCaseGeneratorScreen(
    state: UseCaseUiState,
    onIntent: (UseCaseIntent) -> Unit,
    onBrowseDomainDir: () -> Unit = {}
) {
    val dirState: TextFieldState = rememberTextFieldState()
    val nameState: TextFieldState = rememberTextFieldState()

    LaunchedEffect(state.domainPackage) { dirState.setTextAndPlaceCursorAtEnd(state.domainPackage) }
    LaunchedEffect(state.name) { nameState.setTextAndPlaceCursorAtEnd(state.name) }

    LaunchedEffect(Unit) { snapshotFlow { dirState.text }.collect { onIntent(UseCaseIntent.SetDomainPackage(it.toString())) } }
    LaunchedEffect(Unit) { snapshotFlow { nameState.text }.collect { onIntent(UseCaseIntent.SetName(it.toString())) } }

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
            Text("Domain module directory:")
            TextField(state = dirState, modifier = Modifier.weight(1f))
            DefaultButton(onClick = onBrowseDomainDir) { Text("Browse…") }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("UseCase name (\"UseCase\" will be appended):")
            Spacer(Modifier.width(8.dp))
            TextField(state = nameState)
        }
        val preview = run {
            val raw = state.name.trim()
            if (raw.isEmpty()) "" else "Will generate class: " + (if (raw.endsWith(
                    "UseCase",
                    ignoreCase = true
                )
            ) raw else raw + "UseCase")
        }
        if (preview.isNotEmpty()) {
            Text(preview)
        }
        Text("Repositories (from domain/repository):")
        if (state.availableRepositories.isEmpty()) {
            Text("No repositories found in this feature’s domain module")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.availableRepositories) { repoName ->
                    val checked = state.selectedRepositories.contains(repoName)
                    CheckboxWithLabel(
                        label = repoName,
                        checked = checked,
                        enabled = true,
                        onCheckedChange = { sel -> onIntent(UseCaseIntent.ToggleRepository(repoName, sel)) }
                    )
                }
            }
        }
        Text("Dependency Injection:")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioWithLabel(label = "Metro", selected = state.diMetro, enabled = true) {
                if (!state.diMetro) onIntent(UseCaseIntent.SetDiMetro(true))
            }
            RadioWithLabel(label = "Hilt", selected = state.diHilt, enabled = true) {
                if (!state.diHilt) onIntent(UseCaseIntent.SetDiHilt(true))
            }
            RadioWithLabel(label = "Koin", selected = state.diKoin, enabled = true) {
                if (!state.diKoin) onIntent(UseCaseIntent.SetDiKoin(true))
            }
            if (state.diKoin) {
                CheckboxWithLabel(
                    label = "Koin Annotations",
                    checked = state.diKoinAnnotations,
                    enabled = true,
                    onCheckedChange = { onIntent(UseCaseIntent.ToggleKoinAnnotations(it)) })
            }
        }
        val isDomainModule = state.domainPackage.trimEnd('/', '\\').substringAfterLast('/').substringAfterLast('\\')
            .equals("domain", ignoreCase = true)
        val isValid = state.name.isNotBlank() && state.domainPackage.isNotBlank() && isDomainModule
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DefaultButton(onClick = { onIntent(UseCaseIntent.Generate) }, enabled = isValid && !state.isGenerating) {
                Text(if (state.isGenerating) "Generating…" else "Generate")
            }
        }
        val inlineError =
            if (!isDomainModule && state.domainPackage.isNotBlank()) "Selected directory must be a domain module" else null
        val errorText = state.errorMessage ?: inlineError
        if (errorText != null) {
            Text("Error: $errorText", color = Color(0xFFD32F2F))
        }
        if (state.errorMessage == null && state.lastGeneratedPath != null) {
            Text("Generated at: ${state.lastGeneratedPath}")
        }
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

@Composable
private fun CheckboxWithLabel(label: String, checked: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
