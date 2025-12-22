package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ViewModelRepository
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Implementation of [ViewModelRepository] that generates ViewModel files using KotlinPoet.
 */
class ViewModelRepositoryImpl : ViewModelRepository {

    override fun generateViewModel(
        targetDir: Path,
        packageName: String,
        screenName: String,
        diHilt: Boolean,
        diKoin: Boolean,
        diKoinAnnotations: Boolean,
        patternMVI: Boolean,
        useCaseFqns: List<String>
    ): FileGenerationResult {
        val viewModelName = "${screenName}ViewModel"
        val uiStateName = "${screenName}UiState"
        val intentName = "${screenName}Intent"

        val viewModelClass = ClassName("androidx.lifecycle", "ViewModel")
        val mutableStateFlowClass = ClassName("kotlinx.coroutines.flow", "MutableStateFlow")
        val stateFlowClass = ClassName("kotlinx.coroutines.flow", "StateFlow")
        val asStateFlowMember = com.squareup.kotlinpoet.MemberName("kotlinx.coroutines.flow", "asStateFlow")

        val uiStateType = ClassName(packageName, uiStateName)
        val intentType = ClassName(packageName, intentName)

        val classBuilder = TypeSpec.classBuilder(viewModelName)
            .superclass(viewModelClass)

        // Constructor
        val constructorBuilder = FunSpec.constructorBuilder()
        
        if (diHilt) {
            classBuilder.addAnnotation(ClassName("dagger.hilt.android.lifecycle", "HiltViewModel"))
            constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
        } else if (diKoin) {
            if (diKoinAnnotations) {
                classBuilder.addAnnotation(ClassName("org.koin.android.annotation", "KoinViewModel"))
                classBuilder.addModifiers(KModifier.INTERNAL)
            } else {
                classBuilder.addModifiers(KModifier.INTERNAL)
            }
        }

        useCaseFqns.forEach { fqn ->
            val simpleName = fqn.substringAfterLast('.')
            val pkg = fqn.substringBeforeLast('.', "")
            val paramName = simpleName.replaceFirstChar { it.lowercase() }
            
            val typeName = ClassName(pkg, simpleName)
            
            constructorBuilder.addParameter(paramName, typeName)
            classBuilder.addProperty(
                PropertySpec.builder(paramName, typeName)
                    .initializer(paramName)
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }
        
        classBuilder.primaryConstructor(constructorBuilder.build())

        // State
        val stateProperty = PropertySpec.builder("_state", mutableStateFlowClass.parameterizedBy(uiStateType))
            .addModifiers(KModifier.PRIVATE)
            .initializer("%T(%T())", mutableStateFlowClass, uiStateType)
            .build()
        classBuilder.addProperty(stateProperty)

        val publicStateProperty = PropertySpec.builder("state", stateFlowClass.parameterizedBy(uiStateType))
            .initializer("_state.%M()", asStateFlowMember)
            .build()
        classBuilder.addProperty(publicStateProperty)

        // MVI Intent Handler
        if (patternMVI) {
            val handleIntentFunc = FunSpec.builder("handleIntent")
                .addParameter("intent", intentType)
                .beginControlFlow("when (intent)")
                .addStatement("is %T.OnDismiss -> {}", intentType) // Placeholder
                .endControlFlow()
                .build()
            classBuilder.addFunction(handleIntentFunc)
        }

        val fileSpec = FileSpec.builder(packageName, viewModelName)
            .addType(classBuilder.build())
            .build()
            
        // Post-processing for Koin Annotations backticks
        var fileContent = fileSpec.toString()
        if (diKoin && diKoinAnnotations) {
             fileContent = fileContent.replace("`annotation`", "annotation")
        }

        val outFile = targetDir.resolve("$viewModelName.kt")
        val exists = outFile.exists()
        
        if (!exists) {
            outFile.writeText(fileContent)
        }

        return FileGenerationResult(
            fileName = "$viewModelName.kt",
            path = outFile,
            status = if (exists) GenerationStatus.SKIPPED else GenerationStatus.CREATED
        )
    }
}
