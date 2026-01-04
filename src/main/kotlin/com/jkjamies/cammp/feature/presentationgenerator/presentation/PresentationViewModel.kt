package com.jkjamies.cammp.feature.presentationgenerator.presentation

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.usecase.PresentationGenerator
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
class PresentationViewModel(
    @Assisted private val directory: String,
    @Assisted private val scope: CoroutineScope,
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
        _state.update {
            it.copy(
                isGenerating = true,
                errorMessage = null,
                lastMessage = null
            )
        }

        scope.launch {
            val result = run {
                val patternStrategy =
                    if (s.patternMVI) PresentationPatternStrategy.MVI else PresentationPatternStrategy.MVVM
                val diStrategy = if (s.diKoin) DiStrategy.Koin(s.diKoinAnnotations) else DiStrategy.Hilt

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
