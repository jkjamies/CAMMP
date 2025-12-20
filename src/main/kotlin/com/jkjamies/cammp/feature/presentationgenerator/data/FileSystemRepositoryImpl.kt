package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText

class FileSystemRepositoryImpl : FileSystemRepository {
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
