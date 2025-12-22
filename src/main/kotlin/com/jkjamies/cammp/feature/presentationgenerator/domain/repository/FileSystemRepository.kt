package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import java.nio.file.Path

/**
 * Abstraction for file system operations.
 */
interface FileSystemRepository {
    /**
     * Checks if a file or directory exists at the given path.
     *
     * @param path The path to check.
     * @return True if the path exists, false otherwise.
     */
    fun exists(path: Path): Boolean

    /**
     * Checks if the path is a directory.
     *
     * @param path The path to check.
     * @return True if it is a directory, false otherwise.
     */
    fun isDirectory(path: Path): Boolean

    /**
     * Creates directories at the specified path, including any necessary parent directories.
     *
     * @param path The directory path to create.
     * @return The created path.
     */
    fun createDirectories(path: Path): Path

    /**
     * Writes text content to a file at the specified path.
     *
     * @param path The file path.
     * @param content The text content to write.
     * @param overwriteIfExists Whether to overwrite the file if it already exists.
     */
    fun writeText(path: Path, content: String, overwriteIfExists: Boolean = true)

    /**
     * Reads the text content of a file.
     *
     * @param path The file path.
     * @return The content of the file, or null if it doesn't exist.
     */
    fun readText(path: Path): String?
}
