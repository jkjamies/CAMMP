package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.FlowStateHolderSpecFactory
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FlowStateHolderRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Implementation of [FlowStateHolderRepository] that generates Flow State Holder classes using KotlinPoet.
 */
@ContributesBinding(AppScope::class)
@Inject
class FlowStateHolderRepositoryImpl(
    private val specFactory: FlowStateHolderSpecFactory
) : FlowStateHolderRepository {

    override fun generateFlowStateHolder(
        targetDir: Path,
        packageName: String,
        flowName: String,
        params: PresentationParams
    ): FileGenerationResult {
        val fileName = "$flowName.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val fileSpec = specFactory.create(packageName, flowName)

        target.writeText(fileSpec.toString())
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
