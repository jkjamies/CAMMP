package com.jkjamies.cammp.feature.repositorygenerator.presentation

import com.jkjamies.cammp.feature.repositorygenerator.domain.usecase.RepositoryGenerator
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.usecase.LoadDataSourcesByType
import java.nio.file.Paths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RepositoryViewModel(
    initial: RepositoryUiState = RepositoryUiState(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val generator: RepositoryGenerator = RepositoryGenerator(),
    private val loadDataSourcesByType: LoadDataSourcesByType = LoadDataSourcesByType()
) {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<RepositoryUiState> = _state.asStateFlow()

    fun handleIntent(intent: RepositoryIntent) {
        val s = _state.value
        when (intent) {
            is RepositoryIntent.Generate -> {
                val err = validate(s)
                if (err != null) {
                    _state.update { it.copy(errorMessage = err) }
                    return
                }
                val params = RepositoryParams(
                    dataDir = Paths.get(s.domainPackage),
                    className = s.name,
                    includeDatasource = s.includeDatasource,
                    datasourceCombined = s.datasourceCombined,
                    datasourceRemote = s.datasourceRemote,
                    datasourceLocal = s.datasourceLocal,
                    useKoin = s.diKoin,
                    koinAnnotations = s.diKoinAnnotations,
                    selectedDataSources = s.selectedDataSources.toList().sorted(),
                )
                _state.update { it.copy(isGenerating = true, errorMessage = null, lastGeneratedMessage = null) }
                scope.launch(Dispatchers.IO) {
                    val result = generator(params)
                    val message = result.getOrNull()
                    val error = result.exceptionOrNull()?.message
                    _state.update {
                        it.copy(
                            isGenerating = false,
                            lastGeneratedMessage = message,
                            errorMessage = error,
                        )
                    }
                }
            }
            is RepositoryIntent.SetName -> _state.update { it.copy(name = intent.value) }
            is RepositoryIntent.SetDomainPackage -> {
                val newPath = intent.value
                _state.update { it.copy(domainPackage = newPath) }
                val err = validateDataPath(newPath)
                if (err == null && newPath.isNotBlank()) {
                    scope.launch(Dispatchers.IO) {
                        val map = loadDataSourcesByType(newPath)
                        _state.update { current ->
                            val allowed = current.selectedDataSources.filter { fqn ->
                                map.values.flatten().contains(fqn)
                            }.toSet()
                            current.copy(dataSourcesByType = map, selectedDataSources = allowed)
                        }
                    }
                } else {
                    _state.update { it.copy(dataSourcesByType = emptyMap(), selectedDataSources = emptySet()) }
                }
            }
            is RepositoryIntent.SetGenerateImplementation -> _state.update { it.copy(generateImplementation = intent.selected) }
            is RepositoryIntent.SetIncludeDatasource -> _state.update {
                val enabled = intent.selected
                it.copy(
                    includeDatasource = enabled,
                    datasourceCombined = if (!enabled) it.datasourceCombined else it.datasourceCombined,
                    datasourceRemote = if (!enabled) false else it.datasourceRemote,
                    datasourceLocal = if (!enabled) false else it.datasourceLocal,
                )
            }
            is RepositoryIntent.SetDatasourceCombined -> _state.update {
                val combined = intent.selected
                it.copy(
                    datasourceCombined = combined,
                    datasourceRemote = if (combined) false else it.datasourceRemote,
                    datasourceLocal = if (combined) false else it.datasourceLocal,
                )
            }
            is RepositoryIntent.SetDatasourceRemote -> _state.update {
                val remote = intent.selected
                it.copy(
                    datasourceRemote = remote,
                    datasourceCombined = if (remote || it.datasourceLocal) false else it.datasourceCombined,
                )
            }
            is RepositoryIntent.SetDatasourceLocal -> _state.update {
                val local = intent.selected
                it.copy(
                    datasourceLocal = local,
                    datasourceCombined = if (local || it.datasourceRemote) false else it.datasourceCombined,
                )
            }
            is RepositoryIntent.SetDataSourcesByType -> _state.update { it.copy(dataSourcesByType = intent.value) }
            is RepositoryIntent.ToggleDataSourceSelection -> _state.update { s ->
                val mutable = s.selectedDataSources.toMutableSet()
                if (intent.selected) mutable += intent.fqn else mutable -= intent.fqn
                s.copy(selectedDataSources = mutable)
            }

            is RepositoryIntent.SetDiHilt -> _state.update { it.copy(diHilt = intent.selected, diKoin = !intent.selected).updateDiStates() }
            is RepositoryIntent.SetDiKoin -> _state.update { it.copy(diKoin = intent.selected, diHilt = !intent.selected).updateDiStates() }
            is RepositoryIntent.ToggleKoinAnnotations -> _state.update { it.copy(diKoinAnnotations = intent.selected).updateDiStates() }
        }
    }

    private fun validate(s: RepositoryUiState): String? {
        if (s.name.isBlank()) return "Name is required"
        val path = s.domainPackage
        if (path.isBlank()) return "Data directory is required"
        return validateDataPath(path)
    }

    private fun validateDataPath(path: String): String? {
        val trimmed = path.trimEnd('/', '\\')
        val last = trimmed.substringAfterLast('/').substringAfterLast('\\')
        return if (!last.equals("data", ignoreCase = true)) "Selected directory must be a data module" else null
    }

    private fun RepositoryUiState.updateDiStates(): RepositoryUiState =
        if (diKoin) {
            copy(diHilt = false, diKoin = true, diKoinAnnotations = diKoinAnnotations)
        } else {
            copy(diHilt = true, diKoin = false, diKoinAnnotations = false)
        }
}
