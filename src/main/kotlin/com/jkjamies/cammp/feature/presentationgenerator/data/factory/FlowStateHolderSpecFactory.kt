package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface FlowStateHolderSpecFactory {
    fun create(packageName: String, flowName: String): FileSpec
}

@ContributesBinding(AppScope::class)
@Inject
class FlowStateHolderSpecFactoryImpl : FlowStateHolderSpecFactory {

    override fun create(packageName: String, flowName: String): FileSpec {
        val navHostController = ClassName("androidx.navigation", "NavHostController")
        val rememberNavController = ClassName("androidx.navigation.compose", "rememberNavController")
        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val remember = ClassName("androidx.compose.runtime", "remember")
        val coroutineScope = ClassName("kotlinx.coroutines", "CoroutineScope")
        val rememberCoroutineScope = ClassName("androidx.compose.runtime", "rememberCoroutineScope")

        val classBuilder = TypeSpec.classBuilder(flowName)
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

        val companion = TypeSpec.companionObjectBuilder()
            .addFunction(
                FunSpec.builder("remember${flowName.replace("StateHolder", "")}")
                    .addAnnotation(composableAnnotation)
                    .addParameter("navController", navHostController.copy(nullable = true))
                    .addParameter("scope", coroutineScope.copy(nullable = true))
                    .addStatement(
                        "val navController = navController ?: %M()",
                        rememberNavController
                    )
                    .addStatement("val scope = scope ?: %M()", rememberCoroutineScope)
                    .addStatement(
                        "return %M(navController, scope) { %T(navController, scope) }",
                        remember,
                        ClassName(packageName, flowName)
                    )
                    .build()
            )
            .build()

        classBuilder.addType(companion)

        return FileSpec.builder(packageName, flowName)
            .addType(classBuilder.build())
            .build()
    }
}
