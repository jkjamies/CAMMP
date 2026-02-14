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

package com.jkjamies.cammp.feature.cleanarchitecture.testutil

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AnnotationModuleRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository
import java.nio.file.Path
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

internal class FileSystemRepositoryFake : FileSystemRepository {

    val createdDirectories: MutableSet<Path> = ConcurrentHashMap.newKeySet()
    val writes: MutableMap<Path, String> = ConcurrentHashMap()

    private val existing: MutableSet<Path> = ConcurrentHashMap.newKeySet()

    override fun exists(path: Path): Boolean = path in existing || path in createdDirectories || path in writes

    override fun isDirectory(path: Path): Boolean = path in createdDirectories

    override fun createDirectories(path: Path): Path {
        createdDirectories.add(path)
        existing.add(path)
        return path
    }

    override fun writeText(path: Path, content: String, overwriteIfExists: Boolean) {
        if (!overwriteIfExists && exists(path)) return
        writes[path] = content
        existing.add(path)
    }

    override fun readText(path: Path): String? = writes[path]

    fun markExisting(path: Path, isDir: Boolean = false, content: String? = null) {
        existing.add(path)
        if (isDir) createdDirectories.add(path)
        if (content != null) writes[path] = content
    }
}

internal class TemplateRepositoryFake(
    private val templates: Map<String, String>,
) : TemplateRepository {
    override fun getTemplateText(resourcePath: String): String =
        templates[resourcePath]
            ?: error("Unknown template requested: $resourcePath")
}

internal class AnnotationModuleRepositoryFake : AnnotationModuleRepository {
    data class Call(val outputDirectory: Path, val packageName: String, val scanPackage: String, val featureName: String)
    val calls: MutableList<Call> = Collections.synchronizedList(mutableListOf())

    override fun generate(outputDirectory: Path, packageName: String, scanPackage: String, featureName: String) {
        calls.add(Call(outputDirectory, packageName, scanPackage, featureName))
    }
}
