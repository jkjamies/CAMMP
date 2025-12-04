package com.github.jkjamies.cammp.feature.presentationgenerator.domain.repository

import java.nio.file.Path

interface FileSystemRepository {
    fun exists(path: Path): Boolean
    fun isDirectory(path: Path): Boolean
    fun createDirectories(path: Path): Path
    fun writeText(path: Path, content: String, overwriteIfExists: Boolean = true)
    fun readText(path: Path): String?
}
