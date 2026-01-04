package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import java.nio.file.Path

/**
 * Repository for generating ViewModel files.
 */
interface ViewModelRepository {
    /**
     * Generates a ViewModel class.
     *
     * @param targetDir The directory where the file should be generated.
     * @param packageName The package name for the generated class.
     * @param params The presentation parameters.
     * @return The result of the file generation.
     */
    fun generateViewModel(
        targetDir: Path,
        packageName: String,
        params: PresentationParams
    ): FileGenerationResult
}
