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

package com.jkjamies.cammp.feature.usecasegenerator.di

import com.jkjamies.cammp.feature.usecasegenerator.domain.step.UseCaseStep
import com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

@ContributesTo(AppScope::class)
interface UseCaseGraph {
    val useCaseViewModelFactory: UseCaseViewModel.Factory

    @Multibinds(allowEmpty = true)
    val useCaseSteps: Set<UseCaseStep>
}