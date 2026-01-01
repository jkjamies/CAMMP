package com.jkjamies.cammp.feature.usecasegenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseMergeOutcome
import com.squareup.kotlinpoet.ClassName
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

@ContributesBinding(AppScope::class)
@Inject
class UseCaseDiModuleRepositoryImpl : UseCaseDiModuleRepository {

    override fun mergeUseCaseModule(
        diDir: Path,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        repositoryFqns: List<String>,
        useKoin: Boolean,
    ): UseCaseMergeOutcome {
        if (!useKoin) {
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
        val useCaseClassName = ClassName(useCaseFqn.substringBeforeLast('.'), useCaseSimpleName)
        val repositoryClassNames = repositoryFqns.map { fqn ->
            ClassName(fqn.substringBeforeLast('.'), fqn.substringAfterLast('.'))
        }

        val newBinding = CodeBlock.builder()
            .add("single { %T(", useCaseClassName)
            .apply {
                if (repositoryClassNames.isNotEmpty()) {
                    add(repositoryClassNames.joinToString(", ") { "get()" })
                }
            }
            .add(") }")
            .build()

        val fileSpecBuilder = FileSpec.builder(diPackage, "UseCaseModule")
            .addImport("org.koin.dsl", "module")

        val moduleBlockBuilder = CodeBlock.builder().beginControlFlow("module")

        if (existing != null) {
            // Parse existing imports
            existing.lineSequence()
                .filter { it.trim().startsWith("import ") }
                .map { it.trim().removePrefix("import ").trim() }
                .forEach { importLine ->
                    if (!importLine.contains(" as ")) { // Simple import handling
                        val pkg = importLine.substringBeforeLast('.', "")
                        val name = importLine.substringAfterLast('.')
                        if (pkg.isNotEmpty() && (pkg != "org.koin.dsl" || name != "module")) {
                            fileSpecBuilder.addImport(pkg, name)
                        }
                    }
                }

            // Extract an existing body
            val body = existing.substringAfter("module {", "").substringBeforeLast("}", "")
            if (body.isNotBlank()) {
                // trimIndent() helps preserve relative indentation while removing common leading whitespace
                moduleBlockBuilder.add(CodeBlock.of("%L\n", body.trimIndent()))
            }
        }

        moduleBlockBuilder.addStatement("%L", newBinding)
        moduleBlockBuilder.endControlFlow()

        val fileSpec = fileSpecBuilder
            .addProperty(
                PropertySpec.builder("useCaseModule", ClassName("org.koin.core.module", "Module"))
                    .initializer(moduleBlockBuilder.build())
                    .build()
            )
            .build()

        val content = fileSpec.toString()
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
