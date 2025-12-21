package com.jkjamies.cammp.feature.presentationgenerator.domain.model

import java.nio.file.Path

enum class GenerationStatus {
    CREATED,
    SKIPPED
}

data class FileGenerationResult(
    val path: Path,
    val status: GenerationStatus,
    val fileName: String
)
