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

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface IntentSpecFactory {
    fun create(packageName: String, params: PresentationParams): FileSpec
}

@ContributesBinding(AppScope::class)
class IntentSpecFactoryImpl : IntentSpecFactory {

    override fun create(packageName: String, params: PresentationParams): FileSpec {
        val intentName = "${params.screenName}Intent"
        val sealedInterface = TypeSpec.interfaceBuilder(intentName)
            .addModifiers(KModifier.INTERNAL, KModifier.SEALED)
            .addType(
                TypeSpec.objectBuilder("NoOp")
                    .addSuperinterface(ClassName(packageName, intentName))
                    .build()
            )
            .build()

        return FileSpec.builder(packageName, intentName)
            .addType(sealedInterface)
            .build()
    }
}
