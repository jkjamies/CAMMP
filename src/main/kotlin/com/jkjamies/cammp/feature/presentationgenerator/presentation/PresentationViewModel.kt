package com.jkjamies.cammp.feature.presentationgenerator.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.jkjamies.cammp.feature.presentationgenerator.data.PresentationRepositoryImpl
import com.jkjamies.cammp.feature.presentationgenerator.domain.usecase.PresentationGenerator
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams

class PresentationViewModel(initial: PresentationUiState = PresentationUiState()) {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<PresentationUiState> = _state.asStateFlow()

    private val generator = PresentationGenerator(PresentationRepositoryImpl())

    fun handleIntent(intent: PresentationIntent) {
        when (intent) {
            is PresentationIntent.SetDirectory -> _state.update { it.copy(directory = intent.value) }
            is PresentationIntent.SetScreenName -> _state.update { it.copy(screenName = intent.value) }
            is PresentationIntent.SetPackage -> _state.update { it.copy(pkg = intent.value) }

            is PresentationIntent.ToggleFlowStateHolder -> _state.update { it.copy(useFlowStateHolder = intent.selected) }
            is PresentationIntent.ToggleScreenStateHolder -> _state.update { it.copy(useScreenStateHolder = intent.selected) }
            is PresentationIntent.ToggleIncludeNavigation -> _state.update { it.copy(includeNavigation = intent.selected) }

            is PresentationIntent.SetPatternMVI -> _state.update { it.copy(patternMVI = true, patternMVVM = false) }
            is PresentationIntent.SetPatternMVVM -> _state.update { it.copy(patternMVVM = true, patternMVI = false) }

            is PresentationIntent.SetDiHilt -> _state.update { it.copy(diHilt = true, diKoin = false) }
            is PresentationIntent.SetDiKoin -> _state.update { it.copy(diKoin = true, diHilt = false) }
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
        _state.update { it.copy(isGenerating = true, errorMessage = null, lastMessage = null, lastCreated = emptyList(), lastSkipped = emptyList()) }

        kotlin.runCatching {
            val params = PresentationParams(
                moduleDir = java.nio.file.Paths.get(s.directory),
                screenName = s.screenName,
                patternMVI = s.patternMVI,
                patternMVVM = s.patternMVVM,
                diHilt = s.diHilt,
                diKoin = s.diKoin,
                diKoinAnnotations = s.diKoinAnnotations,
                includeNavigation = s.includeNavigation,
                useFlowStateHolder = s.useFlowStateHolder,
                useScreenStateHolder = s.useScreenStateHolder,
                selectedUseCases = s.selectedUseCases.toList().sorted(),
                overwrite = false,
            )
            generator(params)
        }.fold(
            onSuccess = { result ->
                result.fold(
                    onSuccess = { r ->
                        _state.update {
                            it.copy(
                                isGenerating = false,
                                lastMessage = r.message,
                                lastCreated = r.created,
                                lastSkipped = r.skipped,
                                errorMessage = null,
                            )
                        }
                    },
                    onFailure = { t ->
                        _state.update { it.copy(isGenerating = false, errorMessage = t.message ?: t.toString()) }
                    }
                )
            },
            onFailure = { t ->
                _state.update { it.copy(isGenerating = false, errorMessage = t.message ?: t.toString()) }
            }
        )
    }
}
