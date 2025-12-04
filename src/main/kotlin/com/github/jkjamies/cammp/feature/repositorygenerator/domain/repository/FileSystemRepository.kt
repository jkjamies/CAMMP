package com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository

import java.nio.file.Path

interface FileSystemRepository {
    fun createDirectories(dir: Path)
    fun readFile(path: Path): String?
    fun writeFile(path: Path, content: String)
}

