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

package com.jkjamies.cammp.feature.presentationgenerator.domain.usecase

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.step.PresentationStep
import com.jkjamies.cammp.feature.presentationgenerator.domain.step.StepResult
import dev.zacsweers.metro.Inject

@Inject
class PresentationGenerator(
    private val steps: Set<PresentationStep>
) {
    suspend operator fun invoke(params: PresentationParams): Result<String> = runCatching {
        val results = mutableListOf<String>()
        
        for (step in steps) {
            when (val result = step.execute(params)) {
                is StepResult.Success -> {
                    if (result.message != null) {
                        results.add(result.message)
                    }
                }
                is StepResult.Failure -> throw result.error
            }
        }

        val title = "Presentation generation completed:"
        (sequenceOf(title) + results.asSequence()).joinToString("\n")
    }
}
