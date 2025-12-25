package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenRepository
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Implementation of [ScreenRepository] that generates Composable Screen files using KotlinPoet.
 */
class ScreenRepositoryImpl : ScreenRepository {

    override fun generateScreen(
        targetDir: Path,
        packageName: String,
        screenName: String,
        diHilt: Boolean,
        diKoin: Boolean
    ): FileGenerationResult {
        val viewModelName = "${screenName}ViewModel"
        val viewModelClass = ClassName(packageName, viewModelName)

        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")

        val viewModelInjection = if (diHilt) {
            com.squareup.kotlinpoet.MemberName("androidx.hilt.navigation.compose", "hiltViewModel")
        } else {
            // Koin (both annotated and standard) uses koinViewModel
            com.squareup.kotlinpoet.MemberName("org.koin.compose.viewmodel", "koinViewModel")
        }

        val screenFunc = FunSpec.builder(screenName)
            .addAnnotation(composableAnnotation)
            .addModifiers(KModifier.INTERNAL)
            .addParameter(
                ParameterSpec.builder("viewModel", viewModelClass)
                    .defaultValue("%M()", viewModelInjection)
                    .build()
            )
            .addComment("TODO: implement Screen UI using state from viewModel")
            .build()

        val fileSpec = FileSpec.builder(packageName, screenName)
            .addFunction(screenFunc)
            .build()

        val outFile = targetDir.resolve("$screenName.kt")
        val exists = outFile.exists()

        if (!exists) {
            outFile.writeText(fileSpec.toString())
        }

        return FileGenerationResult(
            fileName = "$screenName.kt",
            path = outFile,
            status = if (exists) GenerationStatus.SKIPPED else GenerationStatus.CREATED
        )
    }
}
