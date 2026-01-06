package com.jkjamies.cammp.feature.repositorygenerator.domain.model

import java.nio.file.Path

sealed interface DiStrategy {
    data object Hilt : DiStrategy
    data class Koin(val useAnnotations: Boolean) : DiStrategy
}

sealed interface DatasourceStrategy {
    data object None : DatasourceStrategy
    data object Combined : DatasourceStrategy
    data object LocalOnly : DatasourceStrategy
    data object RemoteOnly : DatasourceStrategy
    data object RemoteAndLocal : DatasourceStrategy
}

data class RepositoryParams(
    val dataDir: Path,
    val className: String,
    val datasourceStrategy: DatasourceStrategy,
    val diStrategy: DiStrategy,
    val selectedDataSources: List<String> = emptyList(),
)
