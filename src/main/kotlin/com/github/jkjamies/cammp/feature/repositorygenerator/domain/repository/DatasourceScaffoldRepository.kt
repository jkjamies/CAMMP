package com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository

import java.nio.file.Path

data class DatasourceOptions(
    val include: Boolean,
    val combined: Boolean,
    val remote: Boolean,
    val local: Boolean,
    val useKoin: Boolean,
    val koinAnnotations: Boolean,
)

interface DatasourceScaffoldRepository {
    /**
     * Generates datasource interface(s) in the data module and implementation(s) in sibling modules.
     * Optionally merges DI module via the provided flags.
     * Returns a list of human-friendly result lines (paths with statuses).
     */
    fun generate(
        dataDir: Path,
        dataBasePkg: String,
        repositoryBaseName: String,
        diDir: Path?,
        diPackage: String?,
        options: DatasourceOptions,
    ): List<String>
}
