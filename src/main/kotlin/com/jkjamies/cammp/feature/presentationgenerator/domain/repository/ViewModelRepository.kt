package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
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
     * @param screenName The name of the screen.
     * @param diHilt Whether to use Hilt.
     * @param diKoin Whether to use Koin.
     * @param diKoinAnnotations Whether to use Koin annotations.
     * @param patternMVI Whether to use MVI pattern.
     * @param useCaseFqns List of UseCases to inject.
     * @return The result of the file generation.
     */
    fun generateViewModel(
        targetDir: Path,
        packageName: String,
        screenName: String,
        diHilt: Boolean,
        diKoin: Boolean,
        diKoinAnnotations: Boolean,
        patternMVI: Boolean,
        useCaseFqns: List<String>
    ): FileGenerationResult
}
