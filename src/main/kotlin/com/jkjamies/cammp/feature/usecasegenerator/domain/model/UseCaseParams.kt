package com.jkjamies.cammp.feature.usecasegenerator.domain.model

import java.nio.file.Path

sealed interface DiStrategy {
    data object Metro : DiStrategy
    data object Hilt : DiStrategy
    data class Koin(val useAnnotations: Boolean) : DiStrategy
}

data class UseCaseParams(
    val domainDir: Path,
    val className: String,
    val diStrategy: DiStrategy,
    // Selected repositories to inject
    val repositories: List<String> = emptyList(),
)
