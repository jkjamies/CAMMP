package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import java.nio.file.Path

/**
 * Repository for generating UI State files.
 */
interface UiStateRepository {
    /**
     * Generates a UI State data class.
     *
     * @param targetDir The directory where the file should be generated.
     * @param packageName The package name.
     * @param params The presentation parameters.
     * @return The result of the file generation.
     */
    fun generateUiState(
        targetDir: Path,
        packageName: String,
        params: PresentationParams
    ): FileGenerationResult
}
