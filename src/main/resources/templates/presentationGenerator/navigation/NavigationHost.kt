package ${PACKAGE}.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
internal fun ${NAV_HOST_NAME}() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "TODO") {
        // TODO: add destinations
    }
}
