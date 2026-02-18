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
 * Repository for inferring the package name of a module.
 */
interface ModulePackageRepository {
    /**
     * Attempts to detect the base package of the module under [moduleDir].
     *
     * Returns a package that points to the presentation layer when possible
     * (…​.presentation), otherwise best effort based on discovered Kotlin files.
     *
     * @param moduleDir The root directory of the module.
     * @return The inferred package name, or null if it could not be determined.
     */
    fun findModulePackage(moduleDir: Path): String?
}
