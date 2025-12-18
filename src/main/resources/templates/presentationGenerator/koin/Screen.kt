package ${PACKAGE}

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ${SCREEN_NAME}(
    viewModel: ${SCREEN_NAME}ViewModel = koinViewModel()
) {
    // TODO: implement Screen UI using state from viewModel
}
