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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface ScreenSpecFactory {
    fun create(
        packageName: String,
        screenName: String,
        diHilt: Boolean,
        diKoin: Boolean
    ): FileSpec
}

@ContributesBinding(AppScope::class)
class ScreenSpecFactoryImpl : ScreenSpecFactory {

    override fun create(
        packageName: String,
        screenName: String,
        diHilt: Boolean,
        diKoin: Boolean
    ): FileSpec {
        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val viewModelName = "${screenName}ViewModel"
        val viewModelClass = ClassName(packageName, viewModelName)

        val screenFunc = FunSpec.builder(screenName)
            .addAnnotation(composableAnnotation)
            .addModifiers(KModifier.INTERNAL)

        when {
            diHilt -> {
                val hiltViewModel = MemberName("androidx.hilt.lifecycle.viewmodel.compose", "hiltViewModel")
                screenFunc.addParameter(
                    ParameterSpec.builder("viewModel", viewModelClass)
                        .defaultValue("%M()", hiltViewModel)
                        .build()
                )
            }

            diKoin -> {
                val koinViewModel = MemberName("org.koin.androidx.compose", "koinViewModel")
                screenFunc.addParameter(
                    ParameterSpec.builder("viewModel", viewModelClass)
                        .defaultValue("%M()", koinViewModel)
                        .build()
                )
            }

            else -> {
                // Keep compilation valid without DI configured.
                screenFunc.addParameter("viewModel", viewModelClass)
            }
        }

        return FileSpec.builder(packageName, screenName)
            .addFunction(screenFunc.build())
            .build()
    }
}
