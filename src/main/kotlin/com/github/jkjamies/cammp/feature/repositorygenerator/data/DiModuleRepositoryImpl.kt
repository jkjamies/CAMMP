package com.github.jkjamies.cammp.feature.repositorygenerator.data

import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.MergeOutcome
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.FileSystemRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.TemplateRepository
import java.nio.file.Path
import kotlin.io.path.exists

class DiModuleRepositoryImpl(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl(),
    private val templates: TemplateRepository = TemplateRepositoryImpl(),
) : DiModuleRepository {

    private fun extractTemplateImports(templateText: String): Set<String> =
        templateText.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("import ") }
            .toSet()

    private fun extractExistingImports(fileText: String): Set<String> =
        fileText.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("import ") }
            .toSet()

    override fun mergeRepositoryModule(
        diDir: Path,
        diPackage: String,
        className: String,
        domainFqn: String,
        dataFqn: String,
        useKoin: Boolean,
    ): MergeOutcome {
        val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
        fs.createDirectories(diTargetDir)
        val out = diTargetDir.resolve("RepositoryModule.kt")
        val existing = fs.readFile(out)

        val importDomain = "import $domainFqn.$className"
        val importData = "import $dataFqn.${className}Impl"

        if (!useKoin) {
            val baseTemplate = templates.getTemplateText("templates/repositoryGenerator/hilt/RepositoryModule.kt")
            val templateImports = extractTemplateImports(baseTemplate)
            val newBindingBlock = buildString {
                append("    @Binds\n")
                append("    abstract fun bind${className}(repositoryImpl: ${className}Impl): ${className}")
            }
            val bindsSignature = "abstract fun bind${className}(repositoryImpl: ${className}Impl): ${className}"

            val content = if (existing == null) {
                val importsBlock = (sequenceOf(importDomain, importData).toSet() - templateImports)
                    .joinToString("\n")
                baseTemplate
                    .replace("\${PACKAGE}", diPackage)
                    .replace("\${IMPORTS}", if (importsBlock.isBlank()) "" else importsBlock + "\n")
                    .replace("\${BINDINGS}", newBindingBlock)
            } else {
                val currentImports = extractExistingImports(existing)
                val mergedImports = (currentImports + setOf(importDomain, importData)) - templateImports
                val importsBlock = mergedImports.sorted().joinToString("\n")

                var classBody = existing.substringAfter("abstract class RepositoryModule").substringAfter('{').substringBeforeLast('}')
                val hasBinding = classBody.contains(bindsSignature)
                if (!hasBinding) {
                    val trimmedEnd = classBody.trimEnd()
                    classBody = if (trimmedEnd.isBlank()) newBindingBlock else trimmedEnd + "\n\n" + newBindingBlock
                }

                baseTemplate
                    .replace("\${PACKAGE}", diPackage)
                    .replace("\${IMPORTS}", if (importsBlock.isBlank()) "" else importsBlock + "\n")
                    .replace("\${BINDINGS}", classBody.trimEnd())
            }

            val changed = existing == null || existing != content
            fs.writeFile(out, content)
            val status = when {
                existing == null -> "created"
                changed -> "updated"
                else -> "exists"
            }
            return MergeOutcome(out, status)
        } else {
            val baseTemplate = templates.getTemplateText("templates/repositoryGenerator/koin/RepositoryModule.kt")
            val templateImports = extractTemplateImports(baseTemplate)
            val newBindingLine = "    single<$className> { ${className}Impl(get()) }"
            val bindingSignature = "single<$className> { ${className}Impl(get()) }"

            val content = if (existing == null) {
                val importsBlock = (sequenceOf(importDomain, importData).toSet() - templateImports)
                    .joinToString("\n")
                baseTemplate
                    .replace("\${PACKAGE}", diPackage)
                    .replace("\${IMPORTS}", if (importsBlock.isBlank()) "" else importsBlock + "\n")
                    .replace("\${BINDINGS}", newBindingLine)
            } else {
                val currentImports = extractExistingImports(existing)
                val mergedImports = (currentImports + setOf(importDomain, importData)) - templateImports
                val importsBlock = mergedImports.sorted().joinToString("\n")

                var body = existing.substringAfter("module {").substringBeforeLast('}')
                val hasBinding = body.contains(bindingSignature)
                if (!hasBinding) {
                    val trimmedEnd = body.trimEnd()
                    body = if (trimmedEnd.isBlank()) newBindingLine else trimmedEnd + "\n" + newBindingLine
                }

                baseTemplate
                    .replace("\${PACKAGE}", diPackage)
                    .replace("\${IMPORTS}", if (importsBlock.isBlank()) "" else importsBlock + "\n")
                    .replace("\${BINDINGS}", body.trimEnd())
            }

            val changed = existing == null || existing != content
            fs.writeFile(out, content)
            val status = when {
                existing == null -> "created"
                changed -> "updated"
                else -> "exists"
            }
            return MergeOutcome(out, status)
        }
    }

    override fun mergeDataSourceModule(
        diDir: Path,
        diPackage: String,
        desiredBindings: List<DataSourceBinding>,
        useKoin: Boolean,
    ): MergeOutcome {
        val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
        fs.createDirectories(diTargetDir)
        val out = diTargetDir.resolve("DataSourceModule.kt")
        val existing = fs.readFile(out)

        val moduleTemplate = if (useKoin) {
            templates.getTemplateText("templates/dataSourceGenerator/koin/DataSourceModule.kt")
        } else {
            templates.getTemplateText("templates/dataSourceGenerator/hilt/DataSourceModule.kt")
        }

        val content = if (existing == null) {
            val templateImports = extractTemplateImports(moduleTemplate)
            val importsBlock = desiredBindings.flatMap { listOf(it.ifaceImport, it.implImport) }.distinct().joinToString("\n")
            val filteredImports = (importsBlock.lineSequence().map { it.trimEnd() }.toSet() - templateImports)
                .sorted().joinToString("\n")
            val bindsBlock = if (useKoin) desiredBindings.joinToString("\n") { it.block } else desiredBindings.joinToString("\n\n") { it.block }
            moduleTemplate
                .replace("\${PACKAGE}", diPackage)
                .replace("\${IMPORTS}", if (filteredImports.isBlank()) "" else filteredImports + "\n")
                .replace("\${BINDINGS}", bindsBlock)
        } else {
            if (!useKoin) {
                // Merge Hilt using template-driven import exclusion
                val templateImports = extractTemplateImports(moduleTemplate)
                val currentImports = extractExistingImports(existing).toMutableSet()
                currentImports.addAll(desiredBindings.flatMap { listOf(it.ifaceImport, it.implImport) })
                val mergedImports = (currentImports - templateImports).sorted().joinToString("\n")

                var classBody = existing.substringAfter("abstract class DataSourceModule").substringAfter('{').substringBeforeLast('}')
                val existingBody = classBody
                val newBlocks = desiredBindings.filter { !existingBody.contains(it.signature) }.map { it.block }
                if (newBlocks.isNotEmpty()) {
                    val trimmedEnd = classBody.trimEnd()
                    classBody = if (trimmedEnd.isBlank()) newBlocks.joinToString("\n\n") else trimmedEnd + "\n\n" + newBlocks.joinToString("\n\n")
                }

                moduleTemplate
                    .replace("\${PACKAGE}", diPackage)
                    .replace("\${IMPORTS}", if (mergedImports.isBlank()) "" else mergedImports + "\n")
                    .replace("\${BINDINGS}", classBody.trimEnd())
            } else {
                // Merge Koin using template-driven import exclusion
                val templateImports = extractTemplateImports(moduleTemplate)
                val currentImports = extractExistingImports(existing).toMutableSet()
                currentImports.addAll(desiredBindings.flatMap { listOf(it.ifaceImport, it.implImport) })
                val mergedImports = (currentImports - templateImports).sorted().joinToString("\n")

                var body = existing.substringAfter("module {").substringBeforeLast('}')
                val existingBody = body
                val newLines = desiredBindings.filter { !existingBody.contains(it.signature) }.map { it.block }
                if (newLines.isNotEmpty()) {
                    val trimmedEnd = body.trimEnd()
                    body = if (trimmedEnd.isBlank()) newLines.joinToString("\n") else trimmedEnd + "\n" + newLines.joinToString("\n")
                }

                moduleTemplate
                    .replace("\${PACKAGE}", diPackage)
                    .replace("\${IMPORTS}", if (mergedImports.isBlank()) "" else mergedImports + "\n")
                    .replace("\${BINDINGS}", body.trimEnd())
            }
        }

        val changed = existing == null || existing != content
        fs.writeFile(out, content)
        val status = when {
            existing == null -> "created"
            changed -> "updated"
            else -> "exists"
        }
        return MergeOutcome(out, status)
    }
}
