package com.jkjamies.cammp.feature.presentationgenerator.domain.model

import java.nio.file.Path

sealed interface DiStrategy {
    data object Hilt : DiStrategy
    data class Koin(val useAnnotations: Boolean) : DiStrategy
}

sealed interface PresentationPatternStrategy {
    data object MVI : PresentationPatternStrategy
    data object MVVM : PresentationPatternStrategy
}

data class PresentationParams(
    val moduleDir: Path,
    val screenName: String,
    val patternStrategy: PresentationPatternStrategy,
    val diStrategy: DiStrategy,
    val includeNavigation: Boolean = false,
    val useFlowStateHolder: Boolean = false,
    val useScreenStateHolder: Boolean = false,
    val selectedUseCases: List<String> = emptyList(),
)
