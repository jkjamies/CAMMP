package ${PACKAGE}

import androidx.compose.runtime.Immutable

@Immutable
data class ${SCREEN_NAME}UiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)
