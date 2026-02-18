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

package com.jkjamies.cammp.feature.cleanarchitecture.domain.repository

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams

/**
 * Scaffolds the `build-logic/` project used by a clean-architecture feature setup.
 */
interface BuildLogicScaffoldRepository {

    /**
     * Ensures the `build-logic` project exists and contains the required convention plugin sources.
     *
     * @return true if any file or directory was created/updated.
     */
    fun ensureBuildLogic(
        params: CleanArchitectureParams,
        enabledModules: List<String>,
        diMode: DiMode,
    ): Boolean
}
