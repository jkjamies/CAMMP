package com.github.jkjamies.cammp.feature.usecasegenerator.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class UseCaseUiState(
    val name: String = "",
    val domainPackage: String = "",
    val async: Boolean = true,
    val sync: Boolean = false,
    val availableRepositories: List<String> = emptyList(),
    val selectedRepositories: Set<String> = emptySet(),
    val diHilt: Boolean = true,
    val diKoin: Boolean = false,
    val diKoinAnnotations: Boolean = false,
    val isGenerating: Boolean = false,
    val lastGeneratedPath: String? = null,
    val errorMessage: String? = null,
)
