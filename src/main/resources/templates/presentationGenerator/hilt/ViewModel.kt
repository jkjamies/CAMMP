package ${PACKAGE}

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
${IMPORTS}

@HiltViewModel
class ${SCREEN_NAME}ViewModel @Inject constructor(
    ${CONSTRUCTOR_PARAMS}
) : ViewModel() {

    private val _state = MutableStateFlow(${SCREEN_NAME}UiState())
    val state: StateFlow<${SCREEN_NAME}UiState> = _state.asStateFlow()

    ${VIEW_MODEL_INTENT_HANDLER}
}
