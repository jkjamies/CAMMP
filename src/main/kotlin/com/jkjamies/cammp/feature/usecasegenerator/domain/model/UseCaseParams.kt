package com.jkjamies.cammp.feature.usecasegenerator.domain.model

import java.nio.file.Path

data class UseCaseParams(
    val domainDir: Path,
    val className: String,
    val useKoin: Boolean,
    val koinAnnotations: Boolean,
    // Selected repositories to inject
    val repositories: List<String> = emptyList(),
)
