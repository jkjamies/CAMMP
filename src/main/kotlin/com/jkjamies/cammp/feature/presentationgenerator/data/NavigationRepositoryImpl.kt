package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeSpec
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Implementation of [NavigationRepository] that generates Navigation components using KotlinPoet.
 */
class NavigationRepositoryImpl(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl()
) : NavigationRepository {

    override fun generateNavigationHost(
        targetDir: Path,
        packageName: String,
        navHostName: String
    ): FileGenerationResult {
        val fileName = "$navHostName.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val navHost = ClassName("androidx.navigation.compose", "NavHost")
        val rememberNavController = MemberName("androidx.navigation.compose", "rememberNavController")

        val navHostFunction = FunSpec.builder(navHostName)
            .addAnnotation(composableAnnotation)
            .addModifiers(KModifier.INTERNAL)
            .addStatement("val navController = %M()", rememberNavController)
            .addCode(
                CodeBlock.builder()
                    .add("%T(navController = navController, startDestination = %S) {\n", navHost, "TODO")
                    .indent()
                    .add("// TODO: add destinations\n")
                    .unindent()
                    .add("}\n")
                    .build()
            )
            .build()

        val fileSpec = FileSpec.builder(packageName, navHostName)
            .addFunction(navHostFunction)
            .build()

        fs.writeText(target, fileSpec.toString(), overwriteIfExists = false)
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }

    override fun generateDestination(
        targetDir: Path,
        packageName: String,
        screenName: String,
        screenFolder: String
    ): FileGenerationResult {
        val fileName = "${screenName}Destination.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val destinationName = "${screenName}Destination"
        val destinationPackage = "$packageName.navigation.destinations"

        val serializableAnnotation = ClassName("kotlinx.serialization", "Serializable")
        val navGraphBuilder = ClassName("androidx.navigation", "NavGraphBuilder")
        val composableMember = MemberName("androidx.navigation.compose", "composable")

        // Import the screen composable
        val screenComposable = ClassName("$packageName.$screenFolder", screenName)

        val destinationObject = TypeSpec.objectBuilder(destinationName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(serializableAnnotation)
            .build()

        val navGraphExtension = FunSpec.builder(screenName)
            .addModifiers(KModifier.INTERNAL)
            .receiver(navGraphBuilder)
            .addCode(
                CodeBlock.builder()
                    .add("%M<%N> { backStackEntry ->\n", composableMember, destinationName)
                    .indent()
                    .add("%T()\n", screenComposable)
                    .unindent()
                    .add("}\n")
                    .build()
            )
            .build()

        val fileSpec = FileSpec.builder(destinationPackage, fileName)
            .addType(destinationObject)
            .addFunction(navGraphExtension)
            // Add imports for the commented-out examples
            .addImport("androidx.navigation", "NavController", "NavOptionsBuilder", "navOptions", "toRoute")
            .build()

        val content = fileSpec.toString() + getExampleComments(screenName)

        fs.writeText(target, content, overwriteIfExists = false)
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }

    private fun getExampleComments(screenName: String): String {
        return """
            |
            |// example if you need to pass parameters
            |//@Serializable
            |//internal data class ${screenName}Destination(val id: String)
            |
            |// example if navigation host doesn't need to pass a value
            |//internal fun NavGraphBuilder.${screenName}() {
            |//    composable<${screenName}Destination> { backStackEntry ->
            |//        val arguments = backStackEntry.toRoute<${screenName}Destination>()
            |//        ${screenName}(id = arguments.id)
            |//    }
            |//}
            |
            |// example if navigation host needs to pass value and parameter required
            |//internal fun NavGraphBuilder.${screenName}(
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
