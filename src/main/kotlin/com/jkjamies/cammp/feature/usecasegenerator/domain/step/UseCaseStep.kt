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

package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import java.nio.file.Path

sealed interface StepResult {
    data class Success(val path: Path? = null, val message: String? = null) : StepResult
    data class Failure(val error: Throwable) : StepResult
}

interface UseCaseStep {
    suspend fun execute(params: UseCaseParams): StepResult
}
