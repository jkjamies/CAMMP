package com.jkjamies.cammp.feature.repositorygenerator.domain.model

sealed interface DiStrategy {
    data object Hilt : DiStrategy
    data class Koin(val useAnnotations: Boolean) : DiStrategy
}
