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

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.deleteIfExists

/**
 * Test utilities for the cleanarchitecture feature.
 */
object TestFiles {

    /**
     * Creates a temp directory and deletes it recursively after [block] completes.
     *
     * Concurrency-safe as long as each test uses its own temp directory.
     */
    inline fun <T> withTempDir(prefix: String, block: (Path) -> T): T {
        val dir = Files.createTempDirectory(prefix)
        return runCatching { block(dir) }
            .also {
                // Cleanup must never fail a test.
                runCatching { deleteRecursively(dir) }
            }
            .getOrThrow()
    }

    /**
     * Best-effort recursive delete. Does not throw if deletion fails.
     */
    fun deleteRecursively(root: Path) {
        if (!Files.exists(root)) return

        Files.walkFileTree(
            root,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    runCatching { file.deleteIfExists() }
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(dir: Path, exc: java.io.IOException?): FileVisitResult {
                    runCatching { dir.deleteIfExists() }
                    return FileVisitResult.CONTINUE
                }
            }
        )
    }
}
