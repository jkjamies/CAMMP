package com.jkjamies.cammp.feature.cleanarchitecture.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class GenerateModulesUiState(
    val projectBasePath: String? = null,
    val root: String = "",
    val feature: String = "",
    val orgCenter: String = "cammp",

    val platformAndroid: Boolean = true,
    val platformKmp: Boolean = false,

    val includePresentation: Boolean = true,
    val includeApiModule: Boolean = false,
    val includeDiModule: Boolean = true,

    val includeDatasource: Boolean = false,
    val datasourceCombined: Boolean = false,
    val datasourceRemote: Boolean = true,
    val datasourceLocal: Boolean = true,
    val diMetro: Boolean = true,
    val diHilt: Boolean = false,
    val diKoin: Boolean = false,
    val diKoinAnnotations: Boolean = false,

    val orgRightPreview: String = "",
    val isGenerating: Boolean = false,
    val lastCreated: List<String> = emptyList(),
    val lastSkipped: List<String> = emptyList(),
    val settingsUpdated: Boolean? = null,
    val buildLogicCreated: Boolean? = null,
    val lastMessage: String? = null,
    val errorMessage: String? = null,
)
