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

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface UiStateSpecFactory {
    fun create(packageName: String, screenName: String): FileSpec
}

@ContributesBinding(AppScope::class)
internal class UiStateSpecFactoryImpl : UiStateSpecFactory {

    override fun create(packageName: String, screenName: String): FileSpec {
        val uiStateName = "${screenName}UiState"
        val classBuilder = TypeSpec.classBuilder(uiStateName)
            .addModifiers(KModifier.INTERNAL, KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder("isLoading", Boolean::class)
                            .defaultValue("false")
                            .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("isLoading", Boolean::class)
                    .initializer("isLoading")
                    .build()
            )

        return FileSpec.builder(packageName, uiStateName)
            .addType(classBuilder.build())
            .build()
    }
}
