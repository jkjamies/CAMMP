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

package com.jkjamies.cammp.domain.codegen

/**
 * Constants for package name segments used across code generators.
 * Centralising these avoids scattered magic strings when resolving
 * module package paths (e.g. `.domain`, `.data`, `.usecase`).
 */
object PackageSuffixes {
    const val DOMAIN = ".domain"
    const val DATA = ".data"
    const val DI = ".di"
    const val USE_CASE = ".usecase"
    const val REPOSITORY = ".repository"
    const val PRESENTATION = ".presentation"
    const val DATA_SOURCE = ".dataSource"
    const val REMOTE_DATA_SOURCE = ".remoteDataSource"
    const val LOCAL_DATA_SOURCE = ".localDataSource"
    const val NAVIGATION_DESTINATIONS = ".navigation.destinations"
}
