package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import java.nio.file.Path

interface IntentRepository {
    fun generateIntent(
        targetDir: Path,
        packageName: String,
        screenName: String
    ): FileGenerationResult
}
