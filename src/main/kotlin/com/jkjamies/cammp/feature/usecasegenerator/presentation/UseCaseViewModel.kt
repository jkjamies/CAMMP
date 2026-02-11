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

import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.domain.validation.validateModulePath
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.usecase.UseCaseGenerator
import com.jkjamies.cammp.feature.usecasegenerator.domain.usecase.LoadRepositories
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import java.nio.file.Paths
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AssistedInject
class UseCaseViewModel(
    @Assisted private val domainPackage: String,
    @Assisted private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
    private val generator: UseCaseGenerator,
    private val loadRepositories: LoadRepositories
) {
    private val _state = MutableStateFlow(UseCaseUiState(domainPackage = domainPackage))
    val state: StateFlow<UseCaseUiState> = _state.asStateFlow()

    fun handleIntent(intent: UseCaseIntent) {
        val s = _state.value
        when (intent) {
            is UseCaseIntent.Generate -> {
                val err = validate(s)
                if (err != null) {
                    _state.update { it.copy(errorMessage = err) }
                    return
                }
                val normalizedName = normalizeName(s.name)
                val domainDir = Paths.get(s.domainPackage)
                
                val diStrategy = when {
                    s.diKoin -> DiStrategy.Koin(useAnnotations = s.diKoinAnnotations)
                    s.diMetro -> DiStrategy.Metro
                    else -> DiStrategy.Hilt
                }

                val params = UseCaseParams(
                    domainDir = domainDir,
                    className = normalizedName,
                    diStrategy = diStrategy,
                    repositories = s.selectedRepositories.toList().sorted(),
                )
                _state.update { it.copy(isGenerating = true, errorMessage = null, lastGeneratedPath = null) }
                scope.launch {
                    withContext(ioDispatcher) {
                        val result = generator(params)
                        val path = result.getOrNull()
                        val error = result.exceptionOrNull()?.message
                        _state.update {
                            it.copy(
                                isGenerating = false,
                                lastGeneratedPath = path,
                                errorMessage = error,
                            )
                        }
                    }
                }
            }

            is UseCaseIntent.SetName -> _state.update { it.copy(name = intent.value) }
            is UseCaseIntent.SetDomainPackage -> {
                val newPath = intent.value
                val err = validateModulePath(newPath, "domain")
                _state.update { it.copy(domainPackage = newPath, errorMessage = err) }
                if (err == null && newPath.isNotBlank()) {
                    scope.launch {
                        withContext(ioDispatcher) {
                            val repos = loadRepositories(newPath)
                            _state.update { current ->
                                val selected = current.selectedRepositories.filter { it in repos }.toSet()
                                current.copy(availableRepositories = repos, selectedRepositories = selected)
                            }
                        }
                    }
                } else {
                    _state.update { it.copy(availableRepositories = emptyList(), selectedRepositories = emptySet()) }
                }
            }

            is UseCaseIntent.ToggleRepository -> {
                _state.update { s ->
                    val mutable = s.selectedRepositories.toMutableSet()
                    if (intent.selected) mutable += intent.repositoryName else mutable -= intent.repositoryName
                    s.copy(selectedRepositories = mutable)
                }
            }

            is UseCaseIntent.SetAsync -> _state.update { it.copy(async = true, sync = false) }
            is UseCaseIntent.SetSync -> _state.update { it.copy(sync = true, async = false) }
            is UseCaseIntent.SetDiMetro -> _state.update {
                it.copy(diMetro = intent.selected, diHilt = !intent.selected, diKoin = false, diKoinAnnotations = false)
                    .updateDiStates()
            }

            is UseCaseIntent.SetDiHilt -> _state.update {
                it.copy(diHilt = intent.selected, diMetro = !intent.selected, diKoin = false, diKoinAnnotations = false)
                    .updateDiStates()
            }

            is UseCaseIntent.SetDiKoin -> _state.update {
                it.copy(diKoin = intent.selected, diHilt = !intent.selected, diMetro = false).updateDiStates()
            }

            is UseCaseIntent.ToggleKoinAnnotations -> _state.update {
                it.copy(diKoinAnnotations = intent.selected).updateDiStates()
            }
        }
    }

    private fun validate(s: UseCaseUiState): String? {
        if (s.name.isBlank()) return "Name is required"
        val path = s.domainPackage
        if (path.isBlank()) return "Domain directory is required"
        return validateModulePath(path, "domain")
    }

    private fun normalizeName(raw: String): String {
        val trimmed = raw.trim()
        return if (trimmed.endsWith("UseCase")) trimmed else trimmed + "UseCase"
    }

    private fun UseCaseUiState.updateDiStates(): UseCaseUiState =
        when {
            diKoin -> copy(diMetro = false, diHilt = false, diKoin = true, diKoinAnnotations = diKoinAnnotations)
            diMetro -> copy(diMetro = true, diHilt = false, diKoin = false, diKoinAnnotations = false)
            else -> copy(diMetro = false, diHilt = true, diKoin = false, diKoinAnnotations = false)
        }

    @AssistedFactory
    fun interface Factory {
        fun create(domainPackage: String, scope: CoroutineScope): UseCaseViewModel
    }
}
