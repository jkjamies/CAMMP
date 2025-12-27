package com.jkjamies.cammp.feature.cleanarchitecture.fakes

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import java.nio.file.Path

/**
 * A fake implementation of [FileSystemRepository] for testing purposes.
 *
 * This class simulates file system operations in memory, allowing tests to verify
 * file creation and content writing without accessing the actual file system.
 */
class FakeFileSystemRepository : FileSystemRepository {
    /**
     * Stores the content of files written to this fake repository, keyed by their path.
     */
    val writtenFiles = mutableMapOf<Path, String>()

    /**
     * Stores paths that are simulated to exist in the file system.
     */
    val existingPaths = mutableSetOf<Path>()

    /**
     * Stores paths that are simulated to be directories.
     */
    val directories = mutableSetOf<Path>()

    /**
     * Checks if a path exists in the fake file system.
     *
     * @param path The path to check.
     * @return True if the path is in [existingPaths] or [writtenFiles], false otherwise.
     */
    override fun exists(path: Path): Boolean = existingPaths.contains(path) || writtenFiles.containsKey(path)

    /**
     * Checks if a path is a directory in the fake file system.
     *
     * @param path The path to check.
     * @return True if the path is in [directories], false otherwise.
     */
    override fun isDirectory(path: Path): Boolean = directories.contains(path)

    /**
     * Simulates creating directories.
     *
     * Adds the path to [directories] and [existingPaths].
     *
     * @param path The directory path to create.
     * @return The created path.
     */
    override fun createDirectories(path: Path): Path {
        directories.add(path)
        existingPaths.add(path)
        return path
    }

    /**
     * Simulates writing text to a file.
     *
     * Stores the content in [writtenFiles] and adds the path to [existingPaths].
     *
     * @param path The file path.
     * @param content The content to write.
     * @param overwriteIfExists Whether to overwrite if the file exists (ignored in this simple fake, always overwrites).
     */
    override fun writeText(path: Path, content: String, overwriteIfExists: Boolean) {
        writtenFiles[path] = content
        existingPaths.add(path)
    }

    /**
     * Simulates reading text from a file.
     *
     * @param path The file path.
     * @return The content stored in [writtenFiles], or null if not found.
     */
    override fun readText(path: Path): String? = writtenFiles[path]
}
