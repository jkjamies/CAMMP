package com.jkjamies.cammp.feature.presentationgenerator.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class PresentationUiState(
    val directory: String = "",
    val screenName: String = "",
    val pkg: String = "",

    val useFlowStateHolder: Boolean = false,
    val useScreenStateHolder: Boolean = false,
    val includeNavigation: Boolean = false,

    val patternMVI: Boolean = true,
    val patternMVVM: Boolean = false,
    val diHilt: Boolean = true,
    val diKoin: Boolean = false,
    val diKoinAnnotations: Boolean = false,

    val useCasesByModule: Map<String, List<String>> = emptyMap(),
    val selectedUseCases: Set<String> = emptySet(),
    val isGenerating: Boolean = false,
    val lastCreated: List<String> = emptyList(),
    val lastSkipped: List<String> = emptyList(),
    val lastMessage: String? = null,
    val errorMessage: String? = null,
)
