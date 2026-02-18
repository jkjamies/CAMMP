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

package com.jkjamies.cammp.feature.repositorygenerator.domain.usecase

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.step.RepositoryStep
import com.jkjamies.cammp.domain.step.StepResult
import dev.zacsweers.metro.Inject

@Inject
class RepositoryGenerator(
    private val steps: Set<RepositoryStep>
) {
    suspend operator fun invoke(params: RepositoryParams): Result<String> = runCatching {
        val results = mutableListOf<String>()
        
        for (step in steps.sortedBy { it.phase }) {
            when (val result = step.execute(params)) {
                is StepResult.Success -> {
                    val msg = result.message
                    if (msg != null) {
                        results.add(msg)
                    }
                }
                is StepResult.Failure -> throw result.error
            }
        }

        val title = "Repository generation completed:"
        (sequenceOf(title) + results.asSequence()).joinToString("\n")
    }
}
