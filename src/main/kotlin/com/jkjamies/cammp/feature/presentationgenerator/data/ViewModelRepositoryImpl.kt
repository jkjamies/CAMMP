package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.ViewModelSpecFactory
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ViewModelRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Implementation of [ViewModelRepository] that generates ViewModel files using KotlinPoet.
 */
@ContributesBinding(AppScope::class)
class ViewModelRepositoryImpl(
    private val specFactory: ViewModelSpecFactory
) : ViewModelRepository {

    override fun generateViewModel(
        targetDir: Path,
        packageName: String,
        params: PresentationParams
    ): FileGenerationResult {
        val viewModelName = "${params.screenName}ViewModel"
        val fileName = "$viewModelName.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val fileSpec = specFactory.create(packageName, params)
        val content = fileSpec.toString()
        
        target.writeText(content)
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
