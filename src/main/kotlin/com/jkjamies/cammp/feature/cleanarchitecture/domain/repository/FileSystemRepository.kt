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
