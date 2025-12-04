package com.github.jkjamies.cammp.feature.presentationgenerator.domain.model

import java.nio.file.Path

data class PresentationParams(
    val moduleDir: Path,
    val screenName: String,
    // Pattern options
    val patternMVI: Boolean,
    val patternMVVM: Boolean,
    // DI options
    val diHilt: Boolean,
    val diKoin: Boolean,
    val diKoinAnnotations: Boolean,
    // Navigation (Compose Navigation) scaffolding
    val includeNavigation: Boolean = false,
    // State holders
    val useFlowStateHolder: Boolean = false,
    val useScreenStateHolder: Boolean = false,
    // Selected use cases to inject into ViewModel (FQNs or simple names depending on UI)
    val selectedUseCases: List<String> = emptyList(),
    // Overwrite existing generated files
    val overwrite: Boolean = false,
)
