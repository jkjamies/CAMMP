package com.jkjamies.cammp.feature.presentationgenerator.testutil

import java.nio.file.Files
import java.nio.file.Path

/**
 * Test utilities for presentationgenerator tests.
 */
object TestFiles {

    /**
     * Creates a temp directory and deletes it recursively after [block] completes.
     *
     * This is concurrency-safe as long as each test uses its own temp directory.
     */
    inline fun <T> withTempDir(prefix: String, block: (Path) -> T): T {
        val dir = Files.createTempDirectory(prefix)
        return runCatching { block(dir) }
            .also { dir.toFile().deleteRecursively() }
            .getOrThrow()
    }
}

