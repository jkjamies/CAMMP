package com.jkjamies.cammp.feature.usecasegenerator.presentation

sealed interface UseCaseIntent {
    data class SetName(val value: String) : UseCaseIntent
    data class SetDomainPackage(val value: String) : UseCaseIntent
    data class SetAsync(val selected: Boolean) : UseCaseIntent
    data class SetSync(val selected: Boolean) : UseCaseIntent
    data class ToggleRepository(val repositoryName: String, val selected: Boolean) : UseCaseIntent
    data class SetDiMetro(val selected: Boolean) : UseCaseIntent
    data class SetDiHilt(val selected: Boolean) : UseCaseIntent
    data class SetDiKoin(val selected: Boolean) : UseCaseIntent
    data class ToggleKoinAnnotations(val selected: Boolean) : UseCaseIntent
    object Generate : UseCaseIntent
}
