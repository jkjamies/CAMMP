package com.jkjamies.cammp.feature.cleanarchitecture.testutil

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AnnotationModuleRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository
import java.nio.file.Path

internal class FileSystemRepositoryFake : FileSystemRepository {

    val createdDirectories = mutableSetOf<Path>()
    val writes = mutableMapOf<Path, String>()

    private val existing = mutableSetOf<Path>()

    override fun exists(path: Path): Boolean = path in existing || path in createdDirectories || path in writes

    override fun isDirectory(path: Path): Boolean = path in createdDirectories

    override fun createDirectories(path: Path): Path {
        createdDirectories.add(path)
        existing.add(path)
        return path
    }

    override fun writeText(path: Path, content: String, overwriteIfExists: Boolean) {
        if (!overwriteIfExists && exists(path)) return
        writes[path] = content
        existing.add(path)
    }

    override fun readText(path: Path): String? = writes[path]

    fun markExisting(path: Path, isDir: Boolean = false, content: String? = null) {
        existing.add(path)
        if (isDir) createdDirectories.add(path)
        if (content != null) writes[path] = content
    }
}

internal class TemplateRepositoryFake(
    private val templates: Map<String, String>,
) : TemplateRepository {
    override fun getTemplateText(resourcePath: String): String =
        templates[resourcePath]
            ?: error("Unknown template requested: $resourcePath")
}

internal class AnnotationModuleRepositoryFake : AnnotationModuleRepository {
    data class Call(val outputDirectory: Path, val packageName: String, val scanPackage: String, val featureName: String)
    val calls = mutableListOf<Call>()

    override fun generate(outputDirectory: Path, packageName: String, scanPackage: String, featureName: String) {
        calls.add(Call(outputDirectory, packageName, scanPackage, featureName))
    }
}
