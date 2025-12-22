package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
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
     * @param screenName The name of the screen.
     * @param diHilt Whether to use Hilt.
     * @param diKoin Whether to use Koin.
     * @return The result of the file generation.
     */
    fun generateScreen(
        targetDir: Path,
        packageName: String,
        screenName: String,
        diHilt: Boolean,
        diKoin: Boolean
    ): FileGenerationResult
}
