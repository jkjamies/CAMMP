package com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository

import java.nio.file.Path

data class MergeOutcome(val outPath: Path, val status: String)

interface DiModuleRepository {
    /** Create or merge the Repository DI module (Hilt or Koin) and write it. */
    fun mergeRepositoryModule(
        diDir: Path,
        diPackage: String,
        className: String,
        domainFqn: String,
        dataFqn: String,
        useKoin: Boolean,
    ): MergeOutcome

    /** Create or merge the DataSource DI module (Hilt or Koin) and write it. */
    fun mergeDataSourceModule(
        diDir: Path,
        diPackage: String,
        desiredBindings: List<DataSourceBinding>,
        useKoin: Boolean,
    ): MergeOutcome
}

data class DataSourceBinding(
    val ifaceImport: String,
    val implImport: String,
    /** Signature string used to detect duplicates in existing file. */
    val signature: String,
    /** Block line(s) to append for this binding. */
    val block: String,
)
