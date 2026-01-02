package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.IntentRepository
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Implementation of [IntentRepository] that generates MVI Intent interfaces using KotlinPoet.
 */
@ContributesBinding(AppScope::class)
@Inject
class IntentRepositoryImpl(
    private val fs: FileSystemRepository
) : IntentRepository {

    override fun generateIntent(
        targetDir: Path,
        packageName: String,
        screenName: String
    ): FileGenerationResult {
        val intentName = "${screenName}Intent"
        val fileName = "$intentName.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val intentInterface = TypeSpec.interfaceBuilder(intentName)
            .addModifiers(KModifier.INTERNAL, KModifier.SEALED)
            .addType(
                TypeSpec.objectBuilder("NoOp")
                    .addSuperinterface(com.squareup.kotlinpoet.ClassName(packageName, intentName))
                    .build()
            )
            .build()

        val fileSpec = FileSpec.builder(packageName, intentName)
            .addType(intentInterface)
            .build()

        fs.writeText(target, fileSpec.toString(), overwriteIfExists = false)
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
