package com.github.jkjamies.cammp.feature.presentationgenerator.presentation

sealed interface PresentationIntent {
    data class SetDirectory(val value: String) : PresentationIntent
    data class SetScreenName(val value: String) : PresentationIntent
    data class SetPackage(val value: String) : PresentationIntent

    data class ToggleFlowStateHolder(val selected: Boolean) : PresentationIntent
    data class ToggleScreenStateHolder(val selected: Boolean) : PresentationIntent
    data class ToggleIncludeNavigation(val selected: Boolean) : PresentationIntent

    data class SetPatternMVI(val selected: Boolean) : PresentationIntent
    data class SetPatternMVVM(val selected: Boolean) : PresentationIntent

    data class SetDiHilt(val selected: Boolean) : PresentationIntent
    data class SetDiKoin(val selected: Boolean) : PresentationIntent
    data class ToggleKoinAnnotations(val selected: Boolean) : PresentationIntent

    data class SetUseCasesByModule(val value: Map<String, List<String>>) : PresentationIntent
    data class ToggleUseCaseSelection(val fqn: String, val selected: Boolean) : PresentationIntent
    object Generate : PresentationIntent
}
