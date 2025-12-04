package com.github.jkjamies.cammp.feature.usecasegenerator.data

import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseMergeOutcome
import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.TemplateRepository
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class UseCaseDiModuleRepositoryImpl(
    private val templates: TemplateRepository = TemplateRepositoryImpl(),
) : UseCaseDiModuleRepository {

    private fun extractTemplateImports(templateText: String): Set<String> =
        templateText.lineSequence().map { it.trim() }.filter { it.startsWith("import ") }.toSet()

    private fun extractExistingImports(fileText: String): Set<String> =
        fileText.lineSequence().map { it.trim() }.filter { it.startsWith("import ") }.toSet()

    override fun mergeUseCaseModule(
        diDir: Path,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        repositoryFqns: List<String>,
        useKoin: Boolean,
    ): UseCaseMergeOutcome {
        // Only Koin path is supported for UseCase DI modules. Hilt path is intentionally skipped.
        if (!useKoin) {
            // No file writes; report a no-op outcome using the expected path for consistency.
            val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
            val out = diTargetDir.resolve("UseCaseModule.kt")
            return UseCaseMergeOutcome(out, "skipped")
        }

        val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
        if (!diTargetDir.exists()) diTargetDir.createDirectories()
        val out = diTargetDir.resolve("UseCaseModule.kt")
        val existing = if (out.exists()) out.readText() else null

        return mergeKoin(existing, out, diPackage, useCaseSimpleName, useCaseFqn, repositoryFqns)
    }

    private fun mergeKoin(
        existing: String?,
        out: Path,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        repositoryFqns: List<String>,
    ): UseCaseMergeOutcome {
        val baseTemplate = templates.getTemplateText("templates/usecaseGenerator/koin/UseCaseModule.kt")
        val templateImports = extractTemplateImports(baseTemplate)
        val dynamicImports = buildSet {
            add("import $useCaseFqn")
            repositoryFqns.forEach { add("import $it") }
        }
        val bindingLine = buildString {
            append("    single { ")
            append(useCaseSimpleName)
            append("(")
            append(repositoryFqns.joinToString(", ") { "get()" })
            append(") }")
        }
        val signature = "single { $useCaseSimpleName("

        val content = if (existing == null) {
            val importsBlock = (dynamicImports - templateImports).sorted().joinToString("\n")
            baseTemplate
                .replace("\${PACKAGE}", diPackage)
                .replace("\${IMPORTS}", if (importsBlock.isBlank()) "" else importsBlock + "\n")
                .replace("\${BINDINGS}", bindingLine)
        } else {
            val currentImports = extractExistingImports(existing).toMutableSet()
            currentImports.addAll(dynamicImports)
            val importsBlock = (currentImports - templateImports).sorted().joinToString("\n")

            var body = existing.substringAfter("module {").substringBeforeLast('}')
            if (!body.contains(signature)) {
                val trimmed = body.trimEnd()
                body = if (trimmed.isBlank()) bindingLine else trimmed + "\n" + bindingLine
            }

            baseTemplate
                .replace("\${PACKAGE}", diPackage)
                .replace("\${IMPORTS}", if (importsBlock.isBlank()) "" else importsBlock + "\n")
                .replace("\${BINDINGS}", body.trimEnd())
        }

        val changed = existing == null || existing != content
        out.writeText(content)
        val status = when {
            existing == null -> "created"
            changed -> "updated"
            else -> "exists"
        }
        return UseCaseMergeOutcome(out, status)
    }
}
