package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import java.nio.file.Path

/**
 * Repository for generating Screen Composable files.
 */
interface ScreenRepository {
    /**
     * Generates a Screen Composable function.
     *
     * @param targetDir The directory where the file should be generated.
     * @param packageName The package name for the generated file.
     * @param params The presentation parameters.
     * @return The result of the file generation.
     */
    fun generateScreen(
        targetDir: Path,
        packageName: String,
        params: PresentationParams
    ): FileGenerationResult
}
