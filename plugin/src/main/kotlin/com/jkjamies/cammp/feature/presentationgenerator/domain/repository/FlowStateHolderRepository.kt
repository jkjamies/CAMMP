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

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import java.nio.file.Path

/**
 * Repository for generating Flow State Holder files.
 */
interface FlowStateHolderRepository {
    /**
     * Generates a Flow State Holder class.
     *
     * @param targetDir The directory where the file should be generated.
     * @param packageName The package name.
     * @param flowName The name of the flow.
     * @param params The presentation parameters.
     * @return The result of the file generation.
     */
    fun generateFlowStateHolder(
        targetDir: Path,
        packageName: String,
        flowName: String,
        params: PresentationParams
    ): FileGenerationResult
}
