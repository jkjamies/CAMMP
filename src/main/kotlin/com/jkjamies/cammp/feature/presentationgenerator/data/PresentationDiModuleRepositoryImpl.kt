package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationDiModuleRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationMergeOutcome
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.TemplateRepository
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class PresentationDiModuleRepositoryImpl(
    private val templates: TemplateRepository = TemplateRepositoryImpl(),
) : PresentationDiModuleRepository {

    private fun extractTemplateImports(templateText: String): Set<String> =
        templateText.lineSequence().map { it.trim() }.filter { it.startsWith("import ") }.toSet()

    private fun extractExistingImports(fileText: String): Set<String> =
        fileText.lineSequence().map { it.trim() }.filter { it.startsWith("import ") }.toSet()

    override fun mergeViewModelModule(
        diDir: Path,
        diPackage: String,
        viewModelSimpleName: String,
        viewModelFqn: String,
        dependencyCount: Int,
        useKoin: Boolean,
    ): PresentationMergeOutcome {
        // Only Koin supported for ViewModel DI module; Hilt path is a no-op
        if (!useKoin) {
            val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
            val out = diTargetDir.resolve("ViewModelModule.kt")
            return PresentationMergeOutcome(out, "skipped")
        }

        val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
        if (!diTargetDir.exists()) diTargetDir.createDirectories()
        val out = diTargetDir.resolve("ViewModelModule.kt")
        val existing = if (out.exists()) out.readText() else null

        val baseTemplate = templates.getTemplateText("templates/presentationGenerator/koin/ViewModelModule.kt")
        val templateImports = extractTemplateImports(baseTemplate)
        val dynamicImports = setOf("import $viewModelFqn")

        val params = List(dependencyCount) { "get()" }.joinToString(", ")
        val bindingLine = buildString {
            append("    viewModel { ")
            append(viewModelSimpleName)
            append("(")
            append(params)
            append(") }")
        }
        val signature = "viewModel { $viewModelSimpleName("

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
        return PresentationMergeOutcome(out, status)
    }
}
