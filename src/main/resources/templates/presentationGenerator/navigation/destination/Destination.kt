package ${PACKAGE}.navigation.destinations

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import ${PACKAGE}.${SCREEN_FOLDER}.${SCREEN_NAME}

@Serializable
internal object ${SCREEN_NAME}Destination

// example if you need to pass parameters
//@Serializable
//internal data class ${SCREEN_NAME}Destination(val id: String)

// example if no parameters are required
internal fun NavGraphBuilder.${SCREEN_NAME}() {
    composable<${SCREEN_NAME}Destination> { backStackEntry ->
        ${SCREEN_NAME}()
    }
}

// example if navigation host doesn't need o pass a value
//internal fun NavGraphBuilder.${SCREEN_NAME}() {
//    composable<${SCREEN_NAME}Destination> { backStackEntry ->
//        val arguments = backStackEntry.toRoute<${SCREEN_NAME}Destination>()
//        ${SCREEN_NAME}(id = arguments.id)
//    }
//}

// example if navigation host needs to pass value and parameter required
//internal fun NavGraphBuilder.${SCREEN_NAME}(
//    someState: State<String>,
//) {
//    composable<${SCREEN_NAME}Destination> { backStackEntry ->
//        val arguments = backStackEntry.toRoute<${SCREEN_NAME}Destination>()
//        ${SCREEN_NAME}(
//            id = arguments.id,
//            someState = someState
//        )
//    }
//}

// example if simple navigation from this dest
//internal fun NavController.navigateToSomeScreen(
//    builder: NavOptionsBuilder.() -> Unit = {}
//) {
//    this.navigate(SomeScreenDestination, navOptions(builder))
//}

// example if you need to pass parameters
//internal fun NavController.navigateToSomeScreen(
//    userId: String,
//    isAdmin: Boolean = false,
//    builder: NavOptionsBuilder.() -> Unit = {}
//) {
//    val route = SomeScreenDestination(userId = userId, isAdmin = isAdmin)
//    this.navigate(route, navOptions(builder))
//}