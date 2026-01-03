package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.ScreenStateHolderSpecFactory
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenStateHolderRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Implementation of [ScreenStateHolderRepository] that generates Screen State Holder classes using KotlinPoet.
 */
@ContributesBinding(AppScope::class)
@Inject
class ScreenStateHolderRepositoryImpl(
    private val specFactory: ScreenStateHolderSpecFactory
) : ScreenStateHolderRepository {

    override fun generateScreenStateHolder(
        targetDir: Path,
        packageName: String,
        params: PresentationParams
    ): FileGenerationResult {
        val screenName = params.screenName
        val className = "${screenName}StateHolder"
        val fileName = "$className.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val fileSpec = specFactory.create(packageName, screenName)

        target.writeText(fileSpec.toString())
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
