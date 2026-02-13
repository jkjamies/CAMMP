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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface ScreenSpecFactory {
    fun create(
        packageName: String,
        screenName: String,
        diStrategy: DiStrategy,
    ): FileSpec
}

@ContributesBinding(AppScope::class)
internal class ScreenSpecFactoryImpl : ScreenSpecFactory {

    override fun create(
        packageName: String,
        screenName: String,
        diStrategy: DiStrategy,
    ): FileSpec {
        val composableAnnotation = GeneratedAnnotations.COMPOSABLE
        val viewModelName = "${screenName}ViewModel"
        val viewModelClass = ClassName(packageName, viewModelName)

        val screenFunc = FunSpec.builder(screenName)
            .addAnnotation(composableAnnotation)
            .addModifiers(KModifier.INTERNAL)

        when (diStrategy) {
            is DiStrategy.Metro -> {
                val metroViewModel = GeneratedAnnotations.METRO_VIEW_MODEL_COMPOSE
                screenFunc.addParameter(
                    ParameterSpec.builder("viewModel", viewModelClass)
                        .defaultValue("%M()", metroViewModel)
                        .build()
                )
            }

            is DiStrategy.Hilt -> {
                val hiltViewModel = GeneratedAnnotations.HILT_VIEW_MODEL_COMPOSE
                screenFunc.addParameter(
                    ParameterSpec.builder("viewModel", viewModelClass)
                        .defaultValue("%M()", hiltViewModel)
                        .build()
                )
            }

            is DiStrategy.Koin -> {
                val koinViewModel = GeneratedAnnotations.KOIN_VIEW_MODEL_COMPOSE
                screenFunc.addParameter(
                    ParameterSpec.builder("viewModel", viewModelClass)
                        .defaultValue("%M()", koinViewModel)
                        .build()
                )
            }
        }

        return FileSpec.builder(packageName, screenName)
            .addFunction(screenFunc.build())
            .build()
    }
}
