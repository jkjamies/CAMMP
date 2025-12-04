package com.github.jkjamies.cammp.feature.presentationgenerator.domain.model

import java.nio.file.Path

data class PresentationResult(
    val created: List<String>,
    val skipped: List<String>,
    val message: String,
    val outputPaths: List<Path> = emptyList(),
)
