package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenStateHolderRepository
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Implementation of [ScreenStateHolderRepository] that generates Screen State Holder classes using KotlinPoet.
 */
class ScreenStateHolderRepositoryImpl(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl()
) : ScreenStateHolderRepository {

    override fun generateScreenStateHolder(
        targetDir: Path,
        packageName: String,
        screenName: String
    ): FileGenerationResult {
        val className = "${screenName}StateHolder"
        val fileName = "$className.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }
        
        val stateHolderClass = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.INTERNAL)
            .addKdoc("TODO: state for $screenName")
            .build()

        val fileSpec = FileSpec.builder(packageName, className)
            .addType(stateHolderClass)
            .build()

        fs.writeText(target, fileSpec.toString(), overwriteIfExists = false)
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
