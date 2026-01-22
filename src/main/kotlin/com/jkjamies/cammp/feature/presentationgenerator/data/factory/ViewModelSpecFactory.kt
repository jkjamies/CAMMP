package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface ViewModelSpecFactory {
    fun create(packageName: String, params: PresentationParams): FileSpec
}

@ContributesBinding(AppScope::class)
class ViewModelSpecFactoryImpl : ViewModelSpecFactory {

    override fun create(packageName: String, params: PresentationParams): FileSpec {
        val viewModelName = "${params.screenName}ViewModel"
        val uiStateName = "${params.screenName}UiState"
        val intentName = "${params.screenName}Intent"

        val viewModelClass = ClassName("androidx.lifecycle", "ViewModel")
        val mutableStateFlowClass = ClassName("kotlinx.coroutines.flow", "MutableStateFlow")
        val stateFlowClass = ClassName("kotlinx.coroutines.flow", "StateFlow")
        val asStateFlowMember = MemberName("kotlinx.coroutines.flow", "asStateFlow")

        val uiStateType = ClassName(packageName, uiStateName)
        val intentType = ClassName(packageName, intentName)

        val classBuilder = TypeSpec.classBuilder(viewModelName)
            .superclass(viewModelClass)
            .addModifiers(KModifier.INTERNAL)

        // Constructor
        val constructorBuilder = FunSpec.constructorBuilder()

        when (val di = params.diStrategy) {
            is DiStrategy.Metro,
            is DiStrategy.Hilt -> {
                classBuilder.addAnnotation(ClassName("dagger.hilt.android.lifecycle", "HiltViewModel"))
                constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
            }
            is DiStrategy.Koin -> {
                if (di.useAnnotations) {
                    classBuilder.addAnnotation(ClassName("org.koin.android.annotation", "KoinViewModel"))
                }
            }
        }

        params.selectedUseCases.forEach { fqn ->
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
        if (params.patternStrategy is PresentationPatternStrategy.MVI) {
            val handleIntentFunc = FunSpec.builder("handleIntent")
                .addParameter("intent", intentType)
                .beginControlFlow("when (intent)")
                .addStatement("is %T.NoOp -> {}", intentType)
                .endControlFlow()
                .build()
            classBuilder.addFunction(handleIntentFunc)
        }

        return FileSpec.builder(packageName, viewModelName)
            .addType(classBuilder.build())
            .build()
    }
}
