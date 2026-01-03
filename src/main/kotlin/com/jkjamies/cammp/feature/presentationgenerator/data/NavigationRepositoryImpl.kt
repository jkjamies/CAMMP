package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.NavigationSpecFactory
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Implementation of [NavigationRepository] that generates Navigation components using KotlinPoet.
 */
@ContributesBinding(AppScope::class)
@Inject
class NavigationRepositoryImpl(
    private val specFactory: NavigationSpecFactory
) : NavigationRepository {

    override fun generateNavigationHost(
        targetDir: Path,
        packageName: String,
        navHostName: String
    ): FileGenerationResult {
        val fileName = "$navHostName.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val fileSpec = specFactory.createHost(packageName, navHostName)

        target.writeText(fileSpec.toString())
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }

    override fun generateDestination(
        targetDir: Path,
        packageName: String,
        params: PresentationParams,
        screenFolder: String
    ): FileGenerationResult {
        val screenName = params.screenName
        val fileName = "${screenName}Destination.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val fileSpec = specFactory.createDestination(packageName, screenName, screenFolder)

        target.writeText(fileSpec.toString())
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
