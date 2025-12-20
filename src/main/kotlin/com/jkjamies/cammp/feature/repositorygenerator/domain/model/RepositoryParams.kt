package com.jkjamies.cammp.feature.repositorygenerator.domain.model

import java.nio.file.Path

data class RepositoryParams(
    val dataDir: Path,
    val className: String,
    val includeDatasource: Boolean,
    val datasourceCombined: Boolean,
    val datasourceRemote: Boolean,
    val datasourceLocal: Boolean,
    val useKoin: Boolean,
    val koinAnnotations: Boolean,
    val selectedDataSources: List<String> = emptyList(),
)
