package com.github.jkjamies.cammp.feature.cleanarchitecture.domain.model

import java.nio.file.Path

data class CleanArchitectureParams(
    val projectBasePath: Path,
    val root: String,
    val feature: String,
    val orgCenter: String,
    val includePresentation: Boolean,
    val includeDatasource: Boolean,
    val datasourceCombined: Boolean,
    val datasourceRemote: Boolean,
    val datasourceLocal: Boolean,
    val diHilt: Boolean = true,
    val diKoin: Boolean = false,
    val diKoinAnnotations: Boolean = false,
)
