package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationDiModuleRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationMergeOutcome
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Implementation of [PresentationDiModuleRepository] for creating and updating Koin ViewModel modules.
 */
@ContributesBinding(AppScope::class)
class PresentationDiModuleRepositoryImpl : PresentationDiModuleRepository {

    override fun mergeViewModelModule(
        diDir: Path,
        diPackage: String,
        viewModelSimpleName: String,
        viewModelFqn: String,
        dependencyCount: Int,
    ): PresentationMergeOutcome {
        val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
        if (!diTargetDir.exists()) diTargetDir.createDirectories()
        val out = diTargetDir.resolve("ViewModelModule.kt")

        val params = List(dependencyCount) { "get()" }.joinToString(", ")
        val bindingLine = "viewModel { $viewModelSimpleName($params) }"

        if (!out.exists()) {
            val codeBlock = CodeBlock.builder()
                .beginControlFlow("module")
                .addStatement(bindingLine)
                .endControlFlow()
                .build()

            val property = PropertySpec.builder(
                "viewModelModule",
                com.squareup.kotlinpoet.ClassName("org.koin.core.module", "Module")
            )
                .initializer(codeBlock)
                .build()

            val fileSpec = FileSpec.builder(diPackage, "ViewModelModule")
                .addImport("org.koin.androidx.viewmodel.dsl", "viewModel")
                .addImport("org.koin.dsl", "module")
                .addImport(viewModelFqn.substringBeforeLast('.'), viewModelSimpleName)
                .addProperty(property)
                .build()

            out.writeText(fileSpec.toString())
            return PresentationMergeOutcome(out, "created")
        } else {
            val existingText = out.readText()

            if (existingText.contains("viewModel { $viewModelSimpleName(")) {
                return PresentationMergeOutcome(out, "exists")
            }

            val lines = existingText.lines().toMutableList()

            val importLine = "import $viewModelFqn"
            if (!existingText.contains(importLine)) {
                val lastImportIdx = lines.indexOfLast { it.startsWith("import ") }
                if (lastImportIdx != -1) {
                    lines.add(lastImportIdx + 1, importLine)
                } else {
                    val packageIdx = lines.indexOfFirst { it.startsWith("package ") }
                    if (packageIdx != -1) {
                        lines.add(packageIdx + 1, "")
                        lines.add(packageIdx + 2, importLine)
                    } else {
                        lines.add(0, importLine)
                    }
                }
            }

            val moduleStartIdx = lines.indexOfFirst { it.contains("= module {") || it.contains("= module{") }
            if (moduleStartIdx != -1) {
                val lastBraceIdx = lines.indexOfLast { it.contains("}") }
                if (lastBraceIdx != -1) {
                    lines.add(lastBraceIdx, "    $bindingLine")
                }
            }

            out.writeText(lines.joinToString("\n"))
            return PresentationMergeOutcome(out, "updated")
        }
    }
}
