package com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository

import java.nio.file.Path

interface FileSystemRepository {
    fun writeFile(targetDir: Path, fileName: String, content: String): Result<Path>
}
