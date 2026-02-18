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

package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
internal class FileSystemRepositoryImpl : FileSystemRepository {
    override fun exists(path: Path): Boolean = path.exists()
    override fun isDirectory(path: Path): Boolean = path.isDirectory()
    override fun createDirectories(path: Path): Path = path.createDirectories()
    override fun writeText(path: Path, content: String, overwriteIfExists: Boolean) {
        if (!path.parent.exists()) path.parent.createDirectories()
        if (!overwriteIfExists && Files.exists(path)) return
        path.writeText(content)
    }

    override fun readText(path: Path): String? = if (path.exists()) path.readText() else null
}