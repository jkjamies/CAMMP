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

import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.usecase.PresentationGenerator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Paths

@AssistedInject
class PresentationViewModel(
    @Assisted private val directory: String,
    @Assisted private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
    private val generator: PresentationGenerator,
) {
    private val _state = MutableStateFlow(PresentationUiState(directory = directory))
    val state: StateFlow<PresentationUiState> = _state.asStateFlow()

    fun handleIntent(intent: PresentationIntent) {
        when (intent) {
            is PresentationIntent.SetDirectory -> _state.update { it.copy(directory = intent.value) }
            is PresentationIntent.SetScreenName -> _state.update { it.copy(screenName = intent.value) }
            is PresentationIntent.SetPackage -> _state.update { it.copy(pkg = intent.value) }

            is PresentationIntent.ToggleFlowStateHolder -> _state.update { it.copy(useFlowStateHolder = intent.selected) }
            is PresentationIntent.ToggleScreenStateHolder -> _state.update { it.copy(useScreenStateHolder = intent.selected) }
            is PresentationIntent.ToggleIncludeNavigation -> _state.update { it.copy(includeNavigation = intent.selected) }

            is PresentationIntent.SetPatternMVI -> _state.update { it.copy(patternMVI = true, patternMVVM = false, patternCircuit = false) }
            is PresentationIntent.SetPatternMVVM -> _state.update { it.copy(patternMVVM = true, patternMVI = false, patternCircuit = false) }
            is PresentationIntent.SetPatternCircuit -> _state.update { it.copy(patternCircuit = true, patternMVI = false, patternMVVM = false) }

            is PresentationIntent.SetDiMetro -> _state.update { it.copy(diMetro = true, diHilt = false, diKoin = false, diKoinAnnotations = false) }
            is PresentationIntent.SetDiHilt -> _state.update { it.copy(diHilt = true, diMetro = false, diKoin = false, diKoinAnnotations = false) }
            is PresentationIntent.SetDiKoin -> _state.update { it.copy(diKoin = true, diHilt = false, diMetro = false) }
            is PresentationIntent.ToggleKoinAnnotations -> _state.update { it.copy(diKoinAnnotations = intent.selected) }

            is PresentationIntent.SetUseCasesByModule -> _state.update { it.copy(useCasesByModule = intent.value) }
            is PresentationIntent.ToggleUseCaseSelection -> _state.update { current ->
                val sel = current.selectedUseCases.toMutableSet()
                if (intent.selected) sel += intent.fqn else sel -= intent.fqn
                current.copy(selectedUseCases = sel)
            }

            is PresentationIntent.Generate -> generate()
        }
    }

    private fun generate() {
        val s = _state.value
        if (s.directory.isBlank()) {
            _state.update { it.copy(errorMessage = "Directory is required") }
            return
        }
        if (s.screenName.isBlank()) {
            _state.update { it.copy(errorMessage = "Screen name is required") }
            return
        }
        _state.update {
            it.copy(
                isGenerating = true,
                errorMessage = null,
                lastMessage = null
            )
        }

        scope.launch {
            val result = withContext(ioDispatcher) {
                val patternStrategy = when {
                    s.patternMVI -> PresentationPatternStrategy.MVI
                    s.patternCircuit -> PresentationPatternStrategy.Circuit
                    else -> PresentationPatternStrategy.MVVM
                }
                val diStrategy = when {
                    s.diKoin -> DiStrategy.Koin(s.diKoinAnnotations)
                    s.diMetro -> DiStrategy.Metro
                    else -> DiStrategy.Hilt
                }

                val params = PresentationParams(
                    moduleDir = Paths.get(s.directory),
                    screenName = s.screenName,
                    patternStrategy = patternStrategy,
                    diStrategy = diStrategy,
                    includeNavigation = s.includeNavigation,
                    useFlowStateHolder = s.useFlowStateHolder,
                    useScreenStateHolder = s.useScreenStateHolder,
                    selectedUseCases = s.selectedUseCases.toList().sorted(),
                )
                generator(params)
            }

            result.fold(
                onSuccess = { r ->
                    _state.update {
                        it.copy(
                            isGenerating = false,
                            lastMessage = r,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { t ->
                    _state.update { it.copy(isGenerating = false, errorMessage = t.message ?: t.toString()) }
                }
            )
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(directory: String, scope: CoroutineScope): PresentationViewModel
    }
}
