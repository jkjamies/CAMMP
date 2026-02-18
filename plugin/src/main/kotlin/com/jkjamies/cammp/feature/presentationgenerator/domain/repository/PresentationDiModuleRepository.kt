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

package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import java.nio.file.Path

/**
 * Result of merging a ViewModel into a DI module.
 *
 * @property outPath The path to the module file.
 * @property status The status of the operation ("created", "updated", "exists").
 */
data class PresentationMergeOutcome(val outPath: Path, val status: String)

/**
 * Repository for managing DI modules for the presentation layer.
 */
interface PresentationDiModuleRepository {
    /**
     * Create or merge the ViewModel DI module (Koin only) and write it.
     *
     * @param diDir The root directory of the DI module.
     * @param diPackage The package name of the DI module.
     * @param viewModelSimpleName The simple name of the ViewModel.
     * @param viewModelFqn The fully qualified name of the ViewModel.
     * @param dependencyCount The number of dependencies.
     * @return The outcome of the merge operation.
     */
    fun mergeViewModelModule(
        diDir: Path,
        diPackage: String,
        viewModelSimpleName: String,
        viewModelFqn: String,
        dependencyCount: Int,
    ): PresentationMergeOutcome
}
