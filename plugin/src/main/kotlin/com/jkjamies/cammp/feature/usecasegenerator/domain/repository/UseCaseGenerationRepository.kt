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

package com.jkjamies.cammp.feature.usecasegenerator.domain.repository

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import java.nio.file.Path

data class UseCaseGenerationResult(val useCasePath: Path, val interfacePath: Path? = null)

interface UseCaseGenerationRepository {
    /** Generate the UseCase using the given [params] for the provided [packageName]. */
    fun generateUseCase(
        params: UseCaseParams,
        packageName: String,
        baseDomainPackage: String,
        apiDir: Path? = null
    ): UseCaseGenerationResult
}
