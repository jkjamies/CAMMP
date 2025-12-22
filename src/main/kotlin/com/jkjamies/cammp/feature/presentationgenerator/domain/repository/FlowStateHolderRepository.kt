package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import java.nio.file.Path

/**
 * Repository for generating Flow State Holder files.
 */
interface FlowStateHolderRepository {
    /**
     * Generates a Flow State Holder class.
     *
     * @param targetDir The directory where the file should be generated.
     * @param packageName The package name.
     * @param flowName The name of the flow.
     * @return The result of the file generation.
     */
    fun generateFlowStateHolder(
        targetDir: Path,
        packageName: String,
        flowName: String
    ): FileGenerationResult
}
