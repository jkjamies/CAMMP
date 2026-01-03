package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
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
     * @param params The presentation parameters.
     * @return The result of the file generation.
     */
    fun generateIntent(
        targetDir: Path,
        packageName: String,
        params: PresentationParams
    ): FileGenerationResult
}
