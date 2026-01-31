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
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface FlowStateHolderSpecFactory {
    fun create(packageName: String, flowName: String): FileSpec
}

@ContributesBinding(AppScope::class)
class FlowStateHolderSpecFactoryImpl : FlowStateHolderSpecFactory {

    override fun create(packageName: String, flowName: String): FileSpec {
        val navHostController = ClassName("androidx.navigation", "NavHostController")
        val rememberNavController = MemberName("androidx.navigation.compose", "rememberNavController")
        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val remember = MemberName("androidx.compose.runtime", "remember")
        val coroutineScope = ClassName("kotlinx.coroutines", "CoroutineScope")
        val rememberCoroutineScope = MemberName("androidx.compose.runtime", "rememberCoroutineScope")

        val stateHolderClass = TypeSpec.classBuilder(flowName)
            .addModifiers(KModifier.INTERNAL)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("navController", navHostController)
                    .addParameter("scope", coroutineScope)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("navController", navHostController)
                    .initializer("navController")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("scope", coroutineScope)
                    .initializer("scope")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .build()

        // e.g. flowName = ProfileFlowStateHolder -> rememberProfileFlowState
        val rememberFunName = "remember${flowName.removeSuffix("StateHolder")}State"

        val rememberFun = FunSpec.builder(rememberFunName)
            .addAnnotation(composableAnnotation)
            .addModifiers(KModifier.INTERNAL)
            .returns(ClassName(packageName, flowName))
            .addParameter(
                ParameterSpec.builder("navController", navHostController)
                    .defaultValue("%M()", rememberNavController)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("scope", coroutineScope)
                    .defaultValue("%M()", rememberCoroutineScope)
                    .build()
            )
            .addStatement(
                "return %M(\n    navController,\n    scope\n) {\n    %T(\n        navController,\n        scope\n    )\n}",
                remember,
                ClassName(packageName, flowName)
            )
            .build()

        return FileSpec.builder(packageName, flowName)
            .addFunction(rememberFun)
            .addType(stateHolderClass)
            .build()
    }
}
