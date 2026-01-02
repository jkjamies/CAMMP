package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FlowStateHolderRepository
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Implementation of [FlowStateHolderRepository] that generates Flow State Holder classes using KotlinPoet.
 */
@ContributesBinding(AppScope::class)
@Inject
class FlowStateHolderRepositoryImpl(
    private val fs: FileSystemRepository
) : FlowStateHolderRepository {

    override fun generateFlowStateHolder(
        targetDir: Path,
        packageName: String,
        flowName: String
    ): FileGenerationResult {
        val fileName = "$flowName.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val stableAnnotation = ClassName("androidx.compose.runtime", "Stable")
        val rememberFunction = ClassName("androidx.compose.runtime", "remember")
        val navHostControllerClass = ClassName("androidx.navigation", "NavHostController")
        val rememberNavControllerFunction = ClassName("androidx.navigation.compose", "rememberNavController")
        val flowStateClassName = ClassName(packageName, flowName)

        val rememberFun = FunSpec.builder("remember$flowName")
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(composableAnnotation)
            .addParameter(
                ParameterSpec.builder("navController", navHostControllerClass)
                    .defaultValue("%T()", rememberNavControllerFunction)
                    .build()
            )
            .returns(flowStateClassName)
            .beginControlFlow("return %T(navController)", rememberFunction)
            .addStatement("%T(navController)", flowStateClassName)
            .endControlFlow()
            .build()

        val flowClass = TypeSpec.classBuilder(flowName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(stableAnnotation)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("navController", navHostControllerClass)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("navController", navHostControllerClass)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("navController")
                    .build()
            )
            .build()

        val fileSpec = FileSpec.builder(packageName, flowName)
            .addFunction(rememberFun)
            .addType(flowClass)
            .build()

        fs.writeText(target, fileSpec.toString(), overwriteIfExists = false)
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
