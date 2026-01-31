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
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface ScreenStateHolderSpecFactory {
    fun create(packageName: String, screenName: String): FileSpec
}

@ContributesBinding(AppScope::class)
class ScreenStateHolderSpecFactoryImpl : ScreenStateHolderSpecFactory {

    override fun create(packageName: String, screenName: String): FileSpec {
        val stateHolderName = "${screenName}StateHolder"
        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val remember = MemberName("androidx.compose.runtime", "remember")

        val classBuilder = TypeSpec.classBuilder(stateHolderName)
            .addModifiers(KModifier.INTERNAL)

        val rememberFunc = FunSpec.builder("remember${screenName}State")
            .addAnnotation(composableAnnotation)
            .addModifiers(KModifier.INTERNAL)
            .returns(ClassName(packageName, stateHolderName))
            .addStatement("return %M { %T() }", remember, ClassName(packageName, stateHolderName))
            .build()

        return FileSpec.builder(packageName, stateHolderName)
            .addFunction(rememberFunc)
            .addType(classBuilder.build())
            .build()
    }
}
