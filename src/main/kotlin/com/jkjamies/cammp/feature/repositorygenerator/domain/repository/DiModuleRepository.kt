/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkjamies.cammp.feature.repositorygenerator.domain.repository

import java.nio.file.Path

/** Merge outcome model with [outPath] and [status]. */
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

/** Datasource binding model with [ifaceImport], [implImport], [signature], and [block] lines to append. */
data class DataSourceBinding(
    val ifaceImport: String,
    val implImport: String,
    /** Signature string used to detect duplicates in existing file. */
    val signature: String,
    /** Block line(s) to append for this binding. */
    val block: String,
)
