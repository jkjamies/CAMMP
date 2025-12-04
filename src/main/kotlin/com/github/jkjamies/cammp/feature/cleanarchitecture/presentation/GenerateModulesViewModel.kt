package com.github.jkjamies.cammp.feature.cleanarchitecture.presentation

import com.github.jkjamies.cammp.feature.cleanarchitecture.domain.usecase.CleanArchitectureGenerator
import com.github.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.file.Paths

class GenerateModulesViewModel(initial: GenerateModulesUiState) {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<GenerateModulesUiState> = _state.asStateFlow()

    private val generator = CleanArchitectureGenerator()

    fun handleIntent(intent: GenerateModulesIntent) {
        val s = _state.value
        when (intent) {
            is GenerateModulesIntent.SetRoot -> _state.update { it.copy(root = intent.value) }
            is GenerateModulesIntent.SetFeature -> _state.update { it.copy(feature = intent.value) }
            is GenerateModulesIntent.SetOrgCenter -> _state.update { it.copy(orgCenter = intent.value) }

            is GenerateModulesIntent.SetPlatformAndroid -> _state.update { it.copy(platformAndroid = intent.selected, platformKmp = !intent.selected) }
            is GenerateModulesIntent.SetPlatformKmp -> _state.update { it.copy(platformKmp = intent.selected, platformAndroid = !intent.selected) }

            is GenerateModulesIntent.SetIncludePresentation -> _state.update { it.copy(includePresentation = intent.selected) }

            is GenerateModulesIntent.SetIncludeDatasource -> _state.update { it.copy(includeDatasource = intent.selected) }
            is GenerateModulesIntent.SetDatasourceCombined -> _state.update { it.copy(datasourceCombined = intent.selected) }
            is GenerateModulesIntent.SetDatasourceRemote -> _state.update { it.copy(datasourceRemote = intent.selected) }
            is GenerateModulesIntent.SetDatasourceLocal -> _state.update { it.copy(datasourceLocal = intent.selected) }

            is GenerateModulesIntent.SelectDiHilt -> _state.update { it.copy(diHilt = intent.selected, diKoin = !intent.selected, diKoinAnnotations = false) }
            is GenerateModulesIntent.SelectDiKoin -> _state.update { it.copy(diKoin = intent.selected, diHilt = !intent.selected) }
            is GenerateModulesIntent.SetKoinAnnotations -> _state.update { it.copy(diKoinAnnotations = intent.selected) }

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
        _state.update { it.copy(isGenerating = true, errorMessage = null, lastMessage = null, lastCreated = emptyList(), lastSkipped = emptyList(), settingsUpdated = null, buildLogicCreated = null) }

        fun normalizeRoot(projectBase: String, rootInput: String): String {
            val trimmed = rootInput.trim()
            if (trimmed.isEmpty()) return ""
            return try {
                val basePath = Paths.get(projectBase)
                val rootPath = Paths.get(trimmed)
                val rel = if (rootPath.isAbsolute) {
                    if (rootPath.startsWith(basePath)) basePath.relativize(rootPath).toString() else rootPath.fileName.toString()
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

        val result = generator(
            CleanArchitectureParams(
                projectBasePath = Paths.get(base),
                root = rootNormalized,
                feature = featureNormalized,
                orgCenter = s.orgCenter.ifBlank { "cammp" },
                includePresentation = s.includePresentation,
                includeDatasource = s.includeDatasource,
                datasourceCombined = s.datasourceCombined,
                datasourceRemote = s.datasourceRemote,
                datasourceLocal = s.datasourceLocal,
                diHilt = s.diHilt,
                diKoin = s.diKoin,
                diKoinAnnotations = s.diKoinAnnotations,
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
            onFailure = { t -> _state.update { it.copy(isGenerating = false, errorMessage = t.message ?: t.toString(), lastMessage = null) } }
        )
    }
}
