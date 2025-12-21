package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.UiStateRepository
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.nio.file.Path
import kotlin.io.path.exists

class UiStateRepositoryImpl(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl()
) : UiStateRepository {
    override fun generateUiState(
        targetDir: Path,
        packageName: String,
        screenName: String
    ): FileGenerationResult {
        val uiStateName = "${screenName}UiState"
        val fileName = "$uiStateName.kt"
        val target = targetDir.resolve(fileName)
        
        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val uiStateClass = TypeSpec.classBuilder(uiStateName)
            .addModifiers(KModifier.INTERNAL, KModifier.DATA)
            .addAnnotation(ClassName("androidx.compose.runtime", "Immutable"))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder("isLoading", Boolean::class)
                            .defaultValue("false")
                            .build()
                    )
                    .addParameter(
                        ParameterSpec.builder("error", String::class.asTypeName().copy(nullable = true))
                            .defaultValue("null")
                            .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("isLoading", Boolean::class)
                    .initializer("isLoading")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("error", String::class.asTypeName().copy(nullable = true))
                    .initializer("error")
                    .build()
            )
            .build()

        val fileSpec = FileSpec.builder(packageName, uiStateName)
            .addType(uiStateClass)
            .build()

        fs.writeText(target, fileSpec.toString(), overwriteIfExists = false)
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
