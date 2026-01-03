package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface NavigationSpecFactory {
    fun createHost(packageName: String, navHostName: String): FileSpec
    fun createDestination(packageName: String, screenName: String, screenFolder: String): FileSpec
}

@ContributesBinding(AppScope::class)
@Inject
class NavigationSpecFactoryImpl : NavigationSpecFactory {

    override fun createHost(packageName: String, navHostName: String): FileSpec {
        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val modifierClass = ClassName("androidx.compose.ui", "Modifier")
        val navHostController = ClassName("androidx.navigation", "NavHostController")
        val navHost = MemberName("androidx.navigation.compose", "NavHost")

        val funSpec = FunSpec.builder(navHostName)
            .addAnnotation(composableAnnotation)
            .addParameter("navController", navHostController)
            .addParameter("modifier", modifierClass.copy(nullable = true))
            .addCode(
                "// TODO: Replace with your start destination\n%M(navController = navController, startDestination = \"\", modifier = modifier) {\n    // TODO: Add your destinations here\n}",
                navHost
            )
            .build()

        return FileSpec.builder(packageName, navHostName)
            .addFunction(funSpec)
            .build()
    }

    override fun createDestination(packageName: String, screenName: String, screenFolder: String): FileSpec {
        val destinationName = "${screenName}Destination"
        val navGraphBuilder = ClassName("androidx.navigation", "NavGraphBuilder")
        val composableFun = MemberName("androidx.navigation.compose", "composable")
        val screenClass = ClassName("$packageName.$screenFolder", screenName)

        val funSpec = FunSpec.builder(destinationName)
            .receiver(navGraphBuilder)
            .addCode("%M(\"$destinationName\") { %T() }", composableFun, screenClass)
            .build()

        return FileSpec.builder("$packageName.navigation.destinations", destinationName)
            .addFunction(funSpec)
            .build()
    }
}
