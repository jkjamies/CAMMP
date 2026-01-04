package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import java.nio.file.Path

/**
 * Repository for generating Navigation components.
 */
interface NavigationRepository {
    /**
     * Generates a Navigation Host Composable.
     *
     * @param targetDir The directory where the file should be generated.
     * @param packageName The package name.
     * @param navHostName The name of the NavHost.
     * @return The result of the file generation.
     */
    fun generateNavigationHost(
        targetDir: Path,
        packageName: String,
        navHostName: String
    ): FileGenerationResult

    /**
     * Generates a Navigation Destination.
     *
     * @param targetDir The directory where the file should be generated.
     * @param packageName The package name.
     * @param params The presentation parameters.
     * @param screenFolder The folder where the screen resides.
     * @return The result of the file generation.
     */
    fun generateDestination(
        targetDir: Path,
        packageName: String,
        params: PresentationParams,
        screenFolder: String
    ): FileGenerationResult
}
