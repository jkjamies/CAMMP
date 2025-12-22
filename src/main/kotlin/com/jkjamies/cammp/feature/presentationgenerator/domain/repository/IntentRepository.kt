package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import java.nio.file.Path

/**
 * Repository for generating MVI Intent files.
 */
interface IntentRepository {
    /**
     * Generates an MVI Intent interface.
     *
     * @param targetDir The directory where the file should be generated.
     * @param packageName The package name.
     * @param screenName The name of the screen.
     * @return The result of the file generation.
     */
    fun generateIntent(
        targetDir: Path,
        packageName: String,
        screenName: String
    ): FileGenerationResult
}
