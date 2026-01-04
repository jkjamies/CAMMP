package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface NavigationSpecFactory {
    fun createHost(packageName: String, navHostName: String): FileSpec
    fun createDestination(packageName: String, screenName: String, screenFolder: String): FileSpec
    fun getDestinationComments(screenName: String, screenNameLower: String): String
}

@ContributesBinding(AppScope::class)
@Inject
class NavigationSpecFactoryImpl : NavigationSpecFactory {

    override fun createHost(packageName: String, navHostName: String): FileSpec {
        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val navHostController = ClassName("androidx.navigation", "NavHostController")
        val navHost = MemberName("androidx.navigation.compose", "NavHost")
        val rememberNavController = MemberName("androidx.navigation.compose", "rememberNavController")

        val funSpec = FunSpec.builder(navHostName)
            .addAnnotation(composableAnnotation)
            .addModifiers(KModifier.INTERNAL)
            .addCode(
                CodeBlock.builder()
                    .add("// TODO: if using flow state holder, replace with remember function\n")
                    .add("val navController: %T = %M()\n", navHostController, rememberNavController)
                    .add(
                        "// TODO: Replace with your start destination\n%M(navController = navController, startDestination = \"\") {\n    // TODO: Add your destinations here\n}\n",
                        navHost
                    )
                    .build()
            )
            .build()

        return FileSpec.builder(packageName, navHostName)
            .addFunction(funSpec)
            .build()
    }

    override fun createDestination(packageName: String, screenName: String, screenFolder: String): FileSpec {
        val destinationName = "${screenName}Destination"
        val destinationPackage = "$packageName.navigation.destinations"

        val serializableAnnotation = ClassName("kotlinx.serialization", "Serializable")
        val navGraphBuilder = ClassName("androidx.navigation", "NavGraphBuilder")

        val composableMember = MemberName("androidx.navigation.compose", "composable")
        val screenComposable = MemberName("$packageName.$screenFolder", screenName)

        val destinationObject = TypeSpec.objectBuilder(destinationName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(serializableAnnotation)
            .build()

        val screenNameLower = screenName.replaceFirstChar { it.lowercase() }
        val navGraphExtension = FunSpec.builder(screenNameLower)
            .addModifiers(KModifier.INTERNAL)
            .receiver(navGraphBuilder)
            .addCode("%M<%N> { %M() }", composableMember, destinationName, screenComposable)
            .build()

        return FileSpec.builder(destinationPackage, destinationName)
            .addImport("kotlinx.serialization", "Serializable")
            .addType(destinationObject)
            .addFunction(navGraphExtension)
            .addImport("androidx.navigation", "NavController", "NavOptionsBuilder", "navOptions", "toRoute")
            .build()
    }

    override fun getDestinationComments(screenName: String, screenNameLower: String): String {
        return """
            |
            |// example if you need to pass parameters
            |//@Serializable
            |//internal data class ${screenName}Destination(val id: String)
            |
            |// example if navigation host doesn't need to pass a value
            |//internal fun NavGraphBuilder.${screenNameLower}() {
            |//    composable<${screenName}Destination> { backStackEntry ->
            |//        val arguments = backStackEntry.toRoute<${screenName}Destination>()
            |//        ${screenName}(id = arguments.id)
            |//    }
            |//}
            |
            |// example if navigation host needs to pass value and parameter required
            |//internal fun NavGraphBuilder.${screenNameLower}(
            |//    someState: State<String>,
            |//) {
            |//    composable<${screenName}Destination> { backStackEntry ->
            |//        val arguments = backStackEntry.toRoute<${screenName}Destination>()
            |//        ${screenName}(
            |//            id = arguments.id,
            |//            someState = someState
            |//        )
            |//    }
            |//}
            |
            |// example if simple navigation from this dest
            |//internal fun NavController.navigateToSomeScreen(
            |//    builder: NavOptionsBuilder.() -> Unit = {}
            |//) {
            |//    this.navigate(SomeScreenDestination, navOptions(builder))
            |//}
            |
            |// example if you need to pass parameters
            |//internal fun NavController.navigateToSomeScreen(
            |//    userId: String,
            |//    isAdmin: Boolean = false,
            |//    builder: NavOptionsBuilder.() -> Unit = {}
            |//) {
            |//    val route = SomeScreenDestination(userId = userId, isAdmin = isAdmin)
            |//    this.navigate(route, navOptions(builder))
            |//}
        """.trimMargin()
    }
}
