package com.jkjamies.cammp.feature.cleanarchitecture.domain.model

import java.nio.file.Path

sealed interface DiStrategy {
    data object Hilt : DiStrategy
    data object Metro : DiStrategy
    data class Koin(val useAnnotations: Boolean) : DiStrategy
}

sealed interface DatasourceStrategy {
    data object None : DatasourceStrategy
    data object Combined : DatasourceStrategy
    data object LocalOnly : DatasourceStrategy
    data object RemoteOnly : DatasourceStrategy
    data object RemoteAndLocal : DatasourceStrategy
}

data class CleanArchitectureParams(
    val projectBasePath: Path,
    val root: String,
    val feature: String,
    val orgCenter: String,
    val includePresentation: Boolean,
    val includeApiModule: Boolean = false,
    val includeDiModule: Boolean = true,
    val datasourceStrategy: DatasourceStrategy = DatasourceStrategy.None,
    val diStrategy: DiStrategy = DiStrategy.Hilt,
)
