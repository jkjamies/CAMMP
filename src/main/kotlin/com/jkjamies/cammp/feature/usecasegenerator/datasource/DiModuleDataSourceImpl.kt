package com.jkjamies.cammp.feature.usecasegenerator.datasource

import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.DiModuleDataSource
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DiModuleDataSourceImpl : DiModuleDataSource {
    override fun generateKoinModuleContent(
        existingContent: String?,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        repositoryFqns: List<String>
    ): String {
        val useCaseClassName = ClassName(useCaseFqn.substringBeforeLast('.'), useCaseSimpleName)
        val repositoryClassNames = repositoryFqns.map { fqn ->
            ClassName(fqn.substringBeforeLast('.'), fqn.substringAfterLast('.'))
        }

        val newBinding = CodeBlock.Companion.builder()
            .add("single { %T(", useCaseClassName)
            .apply {
                if (repositoryClassNames.isNotEmpty()) {
                    add(repositoryClassNames.joinToString(", ") { "get()" })
                }
            }
            .add(") }")
            .build()

        val fileSpecBuilder = FileSpec.Companion.builder(diPackage, "UseCaseModule")
            .addImport("org.koin.dsl", "module")

        val moduleBlockBuilder = CodeBlock.Companion.builder().beginControlFlow("module")

        if (existingContent != null) {
            // Parse existing imports
            existingContent.lineSequence()
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
            val body = existingContent.substringAfter("module {", "").substringBeforeLast("}", "")
            if (body.isNotBlank()) {
                moduleBlockBuilder.add(CodeBlock.Companion.of("%L\n", body.trimIndent()))
            }
        }

        moduleBlockBuilder.addStatement("%L", newBinding)
        moduleBlockBuilder.endControlFlow()

        val fileSpec = fileSpecBuilder
            .addProperty(
                PropertySpec.Companion.builder("useCaseModule", ClassName("org.koin.core.module", "Module"))
                    .initializer(moduleBlockBuilder.build())
                    .build()
            )
            .build()

        return fileSpec.toString()
    }
}