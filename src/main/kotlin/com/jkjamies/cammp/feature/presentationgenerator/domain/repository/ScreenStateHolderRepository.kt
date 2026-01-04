package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import java.nio.file.Path

/**
 * Repository for generating Screen State Holder files.
 */
interface ScreenStateHolderRepository {
    /**
     * Generates a Screen State Holder class.
     *
     * @param targetDir The directory where the file should be generated.
     * @param packageName The package name.
     * @param params The presentation parameters.
     * @return The result of the file generation.
     */
    fun generateScreenStateHolder(
        targetDir: Path,
        packageName: String,
        params: PresentationParams
    ): FileGenerationResult
}
