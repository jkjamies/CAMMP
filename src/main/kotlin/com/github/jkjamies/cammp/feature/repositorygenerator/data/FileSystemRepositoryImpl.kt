package com.github.jkjamies.cammp.feature.repositorygenerator.data

import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.FileSystemRepository
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class FileSystemRepositoryImpl : FileSystemRepository {
    override fun createDirectories(dir: Path) {
        Files.createDirectories(dir)
    }

    override fun readFile(path: Path): String? = if (Files.exists(path)) Files.readString(path) else null

    override fun writeFile(path: Path, content: String) {
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
}

