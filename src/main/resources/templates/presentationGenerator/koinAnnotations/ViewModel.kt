package ${PACKAGE}

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel
${IMPORTS}

@KoinViewModel
internal class ${SCREEN_NAME}ViewModel(
    ${CONSTRUCTOR_PARAMS}
) : ViewModel() {

    private val _state = MutableStateFlow(${SCREEN_NAME}UiState())
    val state: StateFlow<${SCREEN_NAME}UiState> = _state.asStateFlow()

    ${VIEW_MODEL_INTENT_HANDLER}
}
