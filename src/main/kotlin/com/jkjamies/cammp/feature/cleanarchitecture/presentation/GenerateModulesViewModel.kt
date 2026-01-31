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

package com.jkjamies.cammp.feature.cleanarchitecture.presentation

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.usecase.CleanArchitectureGenerator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Paths

@AssistedInject
class GenerateModulesViewModel(
    @Assisted private val projectBasePath: String,
    @Assisted private val scope: CoroutineScope,
    private val generator: CleanArchitectureGenerator,
) {
    private val _state = MutableStateFlow(
        GenerateModulesUiState(
            projectBasePath = projectBasePath,
            root = projectBasePath,
            orgCenter = projectBasePath.substringAfterLast('/').ifBlank { "cammp" },
            diMetro = true,
            diHilt = false,
            includeDiModule = false,
        )
    )
    val state: StateFlow<GenerateModulesUiState> = _state.asStateFlow()

    fun handleIntent(intent: GenerateModulesIntent) {
        when (intent) {
            is GenerateModulesIntent.SetRoot -> _state.update { it.copy(root = intent.value) }
            is GenerateModulesIntent.SetFeature -> _state.update { it.copy(feature = intent.value) }
            is GenerateModulesIntent.SetOrgCenter -> _state.update { it.copy(orgCenter = intent.value) }

            is GenerateModulesIntent.SetPlatformAndroid -> _state.update {
                it.copy(
                    platformAndroid = intent.selected,
                    platformKmp = !intent.selected
                )
            }

            is GenerateModulesIntent.SetPlatformKmp -> _state.update {
                it.copy(
                    platformKmp = intent.selected,
                    platformAndroid = !intent.selected
                )
            }

            is GenerateModulesIntent.SetIncludePresentation -> _state.update { it.copy(includePresentation = intent.selected) }
            is GenerateModulesIntent.SetIncludeApiModule -> _state.update { it.copy(includeApiModule = intent.selected) }

            is GenerateModulesIntent.SetIncludeDatasource -> _state.update { it.copy(includeDatasource = intent.selected) }
            is GenerateModulesIntent.SetDatasourceCombined -> _state.update { it.copy(datasourceCombined = intent.selected) }
            is GenerateModulesIntent.SetDatasourceRemote -> _state.update { it.copy(datasourceRemote = intent.selected) }
            is GenerateModulesIntent.SetDatasourceLocal -> _state.update { it.copy(datasourceLocal = intent.selected) }

            is GenerateModulesIntent.SelectDiMetro -> _state.update {
                it.copy(
                    diMetro = intent.selected,
                    diHilt = !intent.selected,
                    diKoin = false,
                    diKoinAnnotations = false,
                    includeDiModule = false
                )
            }

            is GenerateModulesIntent.SelectDiHilt -> _state.update {
                it.copy(
                    diHilt = intent.selected,
                    diMetro = !intent.selected,
                    diKoin = false,
                    diKoinAnnotations = false,
                    includeDiModule = true
                )
            }

            is GenerateModulesIntent.SelectDiKoin -> _state.update {
                it.copy(
                    diKoin = intent.selected,
                    diMetro = false,
                    diHilt = !intent.selected,
                    includeDiModule = true
                )
            }

            is GenerateModulesIntent.SetKoinAnnotations -> _state.update {
                it.copy(
                    diKoinAnnotations = intent.selected,
                    includeDiModule = !intent.selected
                )
            }

            is GenerateModulesIntent.SetIncludeDiModule -> _state.update { it.copy(includeDiModule = intent.selected) }

            GenerateModulesIntent.Generate -> generate()
        }
    }

    private fun generate() {
        val s = _state.value
        val base = s.projectBasePath ?: ""
        if (base.isBlank()) {
            _state.update { it.copy(errorMessage = "Project base path is required") }
            return
        }
        if (s.platformKmp) {
            _state.update { it.copy(errorMessage = "KMP generation is not supported yet in CAMMP") }
            return
        }
        if (s.root.isBlank() || s.feature.isBlank()) {
            _state.update { it.copy(errorMessage = "Root and Feature are required") }
            return
        }
        _state.update {
            it.copy(
                isGenerating = true,
                errorMessage = null,
                lastMessage = null,
                lastCreated = emptyList(),
                lastSkipped = emptyList(),
                settingsUpdated = null,
                buildLogicCreated = null
            )
        }

        scope.launch {
            fun normalizeRoot(projectBase: String, rootInput: String): String {
                val trimmed = rootInput.trim()
                if (trimmed.isEmpty()) return ""
                return try {
                    val basePath = Paths.get(projectBase)
                    val rootPath = Paths.get(trimmed)
                    val rel = if (rootPath.isAbsolute) {
                        if (rootPath.startsWith(basePath)) basePath.relativize(rootPath)
                            .toString() else rootPath.fileName.toString()
                    } else trimmed
                    rel.replace('\\', '/').split('/').filter { it.isNotBlank() }.joinToString("/")
                } catch (_: Throwable) {
                    trimmed.replace('\\', '/').substringAfterLast('/')
                }
            }

            fun normalizeFeature(featureInput: String): String {
                val trimmed = featureInput.trim()
                if (trimmed.isEmpty()) return ""
                return trimmed.replace('\\', '/').substringAfterLast('/')
            }

            val rootNormalized = normalizeRoot(base, s.root)
            val featureNormalized = normalizeFeature(s.feature)

            val datasourceStrategy = when {
                !s.includeDatasource -> DatasourceStrategy.None
                s.datasourceCombined -> DatasourceStrategy.Combined
                s.datasourceRemote && s.datasourceLocal -> DatasourceStrategy.RemoteAndLocal
                s.datasourceRemote -> DatasourceStrategy.RemoteOnly
                s.datasourceLocal -> DatasourceStrategy.LocalOnly
                else -> DatasourceStrategy.None
            }

            val result = generator(
                CleanArchitectureParams(
                    projectBasePath = Paths.get(base),
                    root = rootNormalized,
                    feature = featureNormalized,
                    orgCenter = s.orgCenter.ifBlank { "cammp" },
                    includePresentation = s.includePresentation,
                    includeApiModule = s.includeApiModule,
                    includeDiModule = s.includeDiModule,
                    datasourceStrategy = datasourceStrategy,
                    diStrategy = when {
                        s.diKoin -> DiStrategy.Koin(useAnnotations = s.diKoinAnnotations)
                        s.diMetro -> DiStrategy.Metro
                        else -> DiStrategy.Hilt
                    },
                )
            )
            result.fold(
                onSuccess = { r ->
                    _state.update {
                        it.copy(
                            isGenerating = false,
                            lastMessage = r.message,
                            errorMessage = null,
                            lastCreated = r.created,
                            lastSkipped = r.skipped,
                            settingsUpdated = r.settingsUpdated,
                            buildLogicCreated = r.buildLogicCreated,
                        )
                    }
                },
                onFailure = { t ->
                    _state.update {
                        it.copy(
                            isGenerating = false,
                            errorMessage = t.message ?: t.toString(),
                            lastMessage = null
                        )
                    }
                }
            )
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(projectBasePath: String, scope: CoroutineScope): GenerateModulesViewModel
    }
}
