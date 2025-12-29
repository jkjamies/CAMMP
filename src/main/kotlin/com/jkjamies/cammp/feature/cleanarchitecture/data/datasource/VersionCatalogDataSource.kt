package com.jkjamies.cammp.feature.cleanarchitecture.data.datasource

import java.nio.file.Path

interface VersionCatalogDataSource {
    fun getLibraryAlias(
        tomlPath: Path,
        alias: String,
        group: String,
        artifact: String,
        version: String? = null,
        versionRef: String? = null
    ): String

    fun getPluginAlias(
        tomlPath: Path,
        alias: String,
        id: String,
        version: String? = null,
        versionRef: String? = null
    ): String
}
