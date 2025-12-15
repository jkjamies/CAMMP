package ${PACKAGE}

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.annotation.KoinViewModel
${IMPORTS}

@KoinViewModel
class ${SCREEN_NAME}ViewModel(
    ${CONSTRUCTOR_PARAMS}
) : ViewModel() {

    private val _state = MutableStateFlow(${SCREEN_NAME}UiState())
    val state: StateFlow<${SCREEN_NAME}UiState> = _state.asStateFlow()

    ${VIEW_MODEL_INTENT_HANDLER}
}
