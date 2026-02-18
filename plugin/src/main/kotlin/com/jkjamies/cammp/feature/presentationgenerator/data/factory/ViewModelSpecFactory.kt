/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.jkjamies.cammp.domain.codegen.GeneratedAnnotations
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.squareup.kotlinpoet.AnnotationSpec
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
internal class ViewModelSpecFactoryImpl : ViewModelSpecFactory {

    override fun create(packageName: String, params: PresentationParams): FileSpec {
        val viewModelName = "${params.screenName}ViewModel"
        val uiStateName = "${params.screenName}UiState"
        val intentName = "${params.screenName}Intent"

        val viewModelClass = GeneratedAnnotations.VIEW_MODEL
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
            is DiStrategy.Metro -> {
                // @ContributesIntoMap implies @Inject, so no explicit @Inject needed
                classBuilder.addAnnotation(
                    AnnotationSpec.builder(GeneratedAnnotations.METRO_VIEW_MODEL_KEY)
                        .addMember("%T::class", ClassName(packageName, viewModelName))
                        .build()
                )
                classBuilder.addAnnotation(
                    AnnotationSpec.builder(GeneratedAnnotations.METRO_CONTRIBUTES_INTO_MAP)
                        .addMember("%T::class", GeneratedAnnotations.METRO_APP_SCOPE)
                        .build()
                )
            }
            is DiStrategy.Hilt -> {
                classBuilder.addAnnotation(GeneratedAnnotations.HILT_VIEW_MODEL)
                constructorBuilder.addAnnotation(GeneratedAnnotations.JAVAX_INJECT)
            }
            is DiStrategy.Koin -> {
                if (di.useAnnotations) {
                    classBuilder.addAnnotation(GeneratedAnnotations.KOIN_VIEW_MODEL)
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
