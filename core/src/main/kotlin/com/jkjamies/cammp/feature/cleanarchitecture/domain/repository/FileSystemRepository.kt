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

import java.nio.file.Path

interface FileSystemRepository {
    /** Check if [path] exists */
    fun exists(path: Path): Boolean

    /** Check if [path] is a directory */
    fun isDirectory(path: Path): Boolean

    /** Create directories for [path] */
    fun createDirectories(path: Path): Path

    /** Write [content] to [path], overwriting if [overwriteIfExists] is true (default) */
    fun writeText(path: Path, content: String, overwriteIfExists: Boolean = true)

    /** Read the text from [path] */
    fun readText(path: Path): String?
}
