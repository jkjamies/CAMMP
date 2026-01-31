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

package com.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.RepositoryDiscoveryRepository
import dev.zacsweers.metro.Inject

/**
 * Loads the available repository simple names from the specified domain module.
 *
 * @param repo The [RepositoryDiscoveryRepository] to use for loading repositories.
 */
@Inject
class LoadRepositories(
    private val repo: RepositoryDiscoveryRepository
) {
    /**
     * @param domainModulePath The absolute path to the domain module.
     * @return A list of repository simple names.
     */
    operator fun invoke(domainModulePath: String): List<String> = repo.loadRepositories(domainModulePath)
}
