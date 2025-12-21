package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import java.nio.file.Path

interface FlowStateHolderRepository {
    fun generateFlowStateHolder(
        targetDir: Path,
        packageName: String,
        flowName: String
    ): FileGenerationResult
}
