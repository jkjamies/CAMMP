package ${PACKAGE}

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
internal fun ${SCREEN_NAME}(
    viewModel: ${SCREEN_NAME}ViewModel = hiltViewModel()
) {
    // TODO: implement Screen UI using state from viewModel
}
