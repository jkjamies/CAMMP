package com.jkjamies.cammp.feature.repositorygenerator.presentation

sealed interface RepositoryIntent {
    data class SetName(val value: String) : RepositoryIntent
    data class SetDomainPackage(val value: String) : RepositoryIntent
    data class SetGenerateImplementation(val selected: Boolean) : RepositoryIntent
    data class SetIncludeDatasource(val selected: Boolean) : RepositoryIntent
    data class SetDatasourceCombined(val selected: Boolean) : RepositoryIntent
    data class SetDatasourceRemote(val selected: Boolean) : RepositoryIntent
    data class SetDatasourceLocal(val selected: Boolean) : RepositoryIntent
    data class SetDataSourcesByType(val value: Map<String, List<String>>) : RepositoryIntent
    data class ToggleDataSourceSelection(val fqn: String, val selected: Boolean) : RepositoryIntent
    data class SetDiMetro(val selected: Boolean) : RepositoryIntent
    data class SetDiHilt(val selected: Boolean) : RepositoryIntent
    data class SetDiKoin(val selected: Boolean) : RepositoryIntent
    data class ToggleKoinAnnotations(val selected: Boolean) : RepositoryIntent
    object Generate : RepositoryIntent
}
