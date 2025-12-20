package ${PACKAGE}

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * MVI default Screen for ${SCREEN_NAME}.
 * You can wire intents and state once your ViewModel is implemented.
 */
@Composable
internal fun ${SCREEN_NAME}() {
    val viewModel: ${SCREEN_NAME}ViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    // TODO: Render UI using state and dispatch intents to viewModel.onIntent(...)
}
