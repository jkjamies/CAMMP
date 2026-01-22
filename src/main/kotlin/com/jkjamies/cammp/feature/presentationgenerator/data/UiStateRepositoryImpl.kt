package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.UiStateSpecFactory
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.UiStateRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Implementation of [UiStateRepository] that generates UI State data classes using KotlinPoet.
 */
@ContributesBinding(AppScope::class)
class UiStateRepositoryImpl(
    private val specFactory: UiStateSpecFactory
) : UiStateRepository {

    override fun generateUiState(
        targetDir: Path,
        packageName: String,
        params: PresentationParams
    ): FileGenerationResult {
        val uiStateName = "${params.screenName}UiState"
        val fileName = "$uiStateName.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val fileSpec = specFactory.create(packageName, params.screenName)

        target.writeText(fileSpec.toString())
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
