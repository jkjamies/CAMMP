package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import java.nio.file.Path

interface NavigationRepository {
    fun generateNavigationHost(
        targetDir: Path,
        packageName: String,
        navHostName: String
    ): FileGenerationResult

    fun generateDestination(
        targetDir: Path,
        packageName: String,
        screenName: String,
        screenFolder: String
    ): FileGenerationResult
}
