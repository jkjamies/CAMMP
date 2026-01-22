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
