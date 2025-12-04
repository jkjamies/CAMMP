package com.github.jkjamies.cammp.feature.usecasegenerator.data

import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.FileSystemRepository
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

class FileSystemRepositoryImpl : FileSystemRepository {
    override fun writeFile(targetDir: Path, fileName: String, content: String): Result<Path> = runCatching {
        if (!targetDir.exists()) targetDir.createDirectories()
        val file = targetDir.resolve(fileName)
        file.writeText(content)
        file
    }
}
