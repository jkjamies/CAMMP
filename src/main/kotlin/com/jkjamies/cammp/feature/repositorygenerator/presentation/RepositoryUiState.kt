package com.jkjamies.cammp.feature.repositorygenerator.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class RepositoryUiState(
    val name: String = "",
    val domainPackage: String = "",
    val generateImplementation: Boolean = true,
    val includeDatasource: Boolean = false,
    val datasourceCombined: Boolean = false,
    val datasourceRemote: Boolean = true,
    val datasourceLocal: Boolean = true,
    val diHilt: Boolean = true,
    val diKoin: Boolean = false,
    val diKoinAnnotations: Boolean = false,
    val dataSourcesByType: Map<String, List<String>> = emptyMap(),
    val selectedDataSources: Set<String> = emptySet(),
    val isGenerating: Boolean = false,
    val lastGeneratedMessage: String? = null,
    val errorMessage: String? = null,
)
