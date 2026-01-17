package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.ScreenSpecFactory
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Implementation of [ScreenRepository] that generates Composable Screen files using KotlinPoet.
 */
@ContributesBinding(AppScope::class)
@Inject
class ScreenRepositoryImpl(
    private val specFactory: ScreenSpecFactory
) : ScreenRepository {

    override fun generateScreen(
        targetDir: Path,
        packageName: String,
        params: PresentationParams
    ): FileGenerationResult {
        val screenName = params.screenName
        val fileName = "$screenName.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val diHilt = params.diStrategy is DiStrategy.Hilt || params.diStrategy is DiStrategy.Metro
        val diKoin = params.diStrategy is DiStrategy.Koin

        val fileSpec = specFactory.create(packageName, screenName, diHilt, diKoin)

        target.writeText(fileSpec.toString())
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
