package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FlowStateHolderRepository
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Implementation of [FlowStateHolderRepository] that generates Flow State Holder classes using KotlinPoet.
 */
class FlowStateHolderRepositoryImpl(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl()
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

        val mutableStateFlowClass = ClassName("kotlinx.coroutines.flow", "MutableStateFlow")
        val stateFlowClass = ClassName("kotlinx.coroutines.flow", "StateFlow")
        val unitClass = Unit::class.asClassName()
        
        val flowClass = TypeSpec.classBuilder(flowName)
            .addModifiers(KModifier.INTERNAL)
            .addProperty(
                PropertySpec.builder("_state", mutableStateFlowClass.parameterizedBy(unitClass))
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("MutableStateFlow(Unit)")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("state", stateFlowClass.parameterizedBy(unitClass))
                    .initializer("_state")
                    .build()
            )
            .build()

        val fileSpec = FileSpec.builder(packageName, flowName)
            .addType(flowClass)
            .build()

        fs.writeText(target, fileSpec.toString(), overwriteIfExists = false)
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
