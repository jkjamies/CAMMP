package com.jkjamies.cammp.feature.cleanarchitecture.presentation

sealed interface GenerateModulesIntent {
    data class SetRoot(val value: String) : GenerateModulesIntent
    data class SetFeature(val value: String) : GenerateModulesIntent
    data class SetOrgCenter(val value: String) : GenerateModulesIntent
    data class SetPlatformAndroid(val selected: Boolean) : GenerateModulesIntent
    data class SetPlatformKmp(val selected: Boolean) : GenerateModulesIntent
    data class SetIncludePresentation(val selected: Boolean) : GenerateModulesIntent
    data class SetIncludeDatasource(val selected: Boolean) : GenerateModulesIntent
    data class SetDatasourceCombined(val selected: Boolean) : GenerateModulesIntent
    data class SetDatasourceRemote(val selected: Boolean) : GenerateModulesIntent
    data class SetDatasourceLocal(val selected: Boolean) : GenerateModulesIntent
    data class SelectDiHilt(val selected: Boolean) : GenerateModulesIntent
    data class SelectDiKoin(val selected: Boolean) : GenerateModulesIntent
    data class SetKoinAnnotations(val selected: Boolean) : GenerateModulesIntent
    object Generate : GenerateModulesIntent
}
