package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.MergeOutcome
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class DiModuleRepositoryImpl : DiModuleRepository {

    override fun mergeRepositoryModule(
        diDir: Path,
        diPackage: String,
        className: String,
        domainFqn: String,
        dataFqn: String,
        useKoin: Boolean,
    ): MergeOutcome {
        val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
        diTargetDir.createDirectories()
        val out = diTargetDir.resolve("RepositoryModule.kt")
        val existing = if (out.exists()) out.readText() else null

        val domainClassName = ClassName(domainFqn, className)
        val dataClassName = ClassName(dataFqn, "${className}Impl")

        val fileSpec = if (useKoin) {
            createKoinModule(
                diPackage,
                "RepositoryModule",
                "repositoryModule",
                existing,
                "single<%T> { %T(get()) }",
                domainClassName,
                dataClassName
            )
        } else {
            createHiltModule(
                diPackage,
                "RepositoryModule",
                existing,
                "bind${className}",
                dataClassName,
                domainClassName
            )
        }

        val content = fileSpec.toString().replace("`data`", "data")
        val changed = existing == null || existing != content
        out.writeText(content)
        val status = when {
            existing == null -> "created"
            changed -> "updated"
            else -> "exists"
        }
        return MergeOutcome(out, status)
    }

    override fun mergeDataSourceModule(
        diDir: Path,
        diPackage: String,
        desiredBindings: List<DataSourceBinding>,
        useKoin: Boolean,
    ): MergeOutcome {
        val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
        diTargetDir.createDirectories()
        val out = diTargetDir.resolve("DataSourceModule.kt")
        val existing = if (out.exists()) out.readText() else null

        val fileSpec = if (useKoin) {
            createKoinDataSourceModule(diPackage, "DataSourceModule", "dataSourceModule", existing, desiredBindings)
        } else {
            createHiltDataSourceModule(diPackage, "DataSourceModule", existing, desiredBindings)
        }

        val content = fileSpec.toString().replace("`data`", "data")
        val changed = existing == null || existing != content
        out.writeText(content)
        val status = when {
            existing == null -> "created"
            changed -> "updated"
            else -> "exists"
        }
        return MergeOutcome(out, status)
    }

    private fun createKoinModule(
        packageName: String,
        fileName: String,
        propertyName: String,
        existingContent: String?,
        bindingFormat: String,
        vararg classNames: ClassName
    ): FileSpec {
        val moduleBlock = CodeBlock.builder()
        if (existingContent != null) {
            val body = existingContent.substringAfter("module {").substringBeforeLast("}")
            if (body.isNotBlank()) {
                moduleBlock.add(body.trimIndent())
                moduleBlock.add("\n")
            }
        }
        moduleBlock.addStatement(bindingFormat, *classNames)

        return FileSpec.builder(packageName, fileName)
            .addImport("org.koin.dsl", "module")
            .addImport("org.koin.core.module", "Module")
            .addProperty(
                PropertySpec.builder(propertyName, ClassName("org.koin.core.module", "Module"))
                    .initializer(
                        CodeBlock.builder().beginControlFlow("module").add(moduleBlock.build()).endControlFlow().build()
                    )
                    .build()
            )
            .build()
    }

    private fun createHiltModule(
        packageName: String,
        fileName: String,
        existingContent: String?,
        bindingFunctionName: String,
        implClassName: ClassName,
        ifaceClassName: ClassName
    ): FileSpec {
        val classBuilder = TypeSpec.classBuilder(fileName)
            .addModifiers(KModifier.ABSTRACT)
            .addAnnotation(ClassName("dagger", "Module"))
            .addAnnotation(
                AnnotationSpec.builder(ClassName("dagger.hilt", "InstallIn"))
                    .addMember("%T::class", ClassName("dagger.hilt.components", "SingletonComponent"))
                    .build()
            )

        val existingFunctions = mutableSetOf<String>()
        if (existingContent != null) {
            val body = existingContent.substringAfter("abstract class $fileName {").substringBeforeLast("}")
            if (body.isNotBlank()) {
                // This is a simplified way to add existing functions. A more robust solution would parse the existing file.
                // For now, we just add the raw body. This will not handle imports correctly.
                body.lines().forEach { line ->
                    if (line.contains("fun bind")) {
                        existingFunctions.add(line.trim())
                    }
                }
            }
        }

        existingFunctions.forEach {
            // This is a hacky way to add existing functions. A proper solution would parse the function signature.
            // For now, we assume a simple structure.
            val funName = it.substringAfter("fun ").substringBefore("(")
            val param = it.substringAfter("(").substringBefore(")")
            val paramName = param.substringBefore(":").trim()
            val paramType = param.substringAfter(":").trim()
            val returnType = it.substringAfter("):").trim()
            classBuilder.addFunction(
                FunSpec.builder(funName)
                    .addModifiers(KModifier.ABSTRACT)
                    .addAnnotation(ClassName("dagger", "Binds"))
                    .addParameter(
                        paramName,
                        ClassName(paramType.substringBeforeLast("."), paramType.substringAfterLast("."))
                    )
                    .returns(ClassName(returnType.substringBeforeLast("."), returnType.substringAfterLast(".")))
                    .build()
            )
        }

        if (!existingFunctions.any { it.contains(bindingFunctionName) }) {
            classBuilder.addFunction(
                FunSpec.builder(bindingFunctionName)
                    .addModifiers(KModifier.ABSTRACT)
                    .addAnnotation(ClassName("dagger", "Binds"))
                    .addParameter("repositoryImpl", implClassName)
                    .returns(ifaceClassName)
                    .build()
            )
        }

        return FileSpec.builder(packageName, fileName)
            .addType(classBuilder.build())
            .build()
    }

    private fun createKoinDataSourceModule(
        packageName: String,
        fileName: String,
        propertyName: String,
        existingContent: String?,
        bindings: List<DataSourceBinding>
    ): FileSpec {
        val moduleBlock = CodeBlock.builder()
        if (existingContent != null) {
            val body = existingContent.substringAfter("module {").substringBeforeLast("}")
            if (body.isNotBlank()) {
                moduleBlock.add(body.trimIndent())
                moduleBlock.add("\n")
            }
        }

        bindings.forEach { binding ->
            val iface = ClassName(
                binding.ifaceImport.removePrefix("import ").substringBeforeLast("."),
                binding.ifaceImport.substringAfterLast(".")
            )
            val impl = ClassName(
                binding.implImport.removePrefix("import ").substringBeforeLast("."),
                binding.implImport.substringAfterLast(".")
            )
            moduleBlock.addStatement("single<%T> { %T(get()) }", iface, impl)
        }

        return FileSpec.builder(packageName, fileName)
            .addImport("org.koin.dsl", "module")
            .addImport("org.koin.core.module", "Module")
            .addProperty(
                PropertySpec.builder(propertyName, ClassName("org.koin.core.module", "Module"))
                    .initializer(
                        CodeBlock.builder().beginControlFlow("module").add(moduleBlock.build()).endControlFlow().build()
                    )
                    .build()
            )
            .build()
    }

    private fun createHiltDataSourceModule(
        packageName: String,
        fileName: String,
        existingContent: String?,
        bindings: List<DataSourceBinding>
    ): FileSpec {
        val classBuilder = TypeSpec.classBuilder(fileName)
            .addModifiers(KModifier.ABSTRACT)
            .addAnnotation(ClassName("dagger", "Module"))
            .addAnnotation(
                AnnotationSpec.builder(ClassName("dagger.hilt", "InstallIn"))
                    .addMember("%T::class", ClassName("dagger.hilt.components", "SingletonComponent"))
                    .build()
            )

        val existingFunctions = mutableSetOf<String>()
        if (existingContent != null) {
            val body = existingContent.substringAfter("abstract class $fileName {").substringBeforeLast("}")
            if (body.isNotBlank()) {
                body.lines().forEach { line ->
                    if (line.contains("fun bind")) {
                        existingFunctions.add(line.trim())
                    }
                }
            }
        }

        existingFunctions.forEach {
            val funName = it.substringAfter("fun ").substringBefore("(")
            val param = it.substringAfter("(").substringBefore(")")
            val paramName = param.substringBefore(":").trim()
            val paramType = param.substringAfter(":").trim()
            val returnType = it.substringAfter("):").trim()
            classBuilder.addFunction(
                FunSpec.builder(funName)
                    .addModifiers(KModifier.ABSTRACT)
                    .addAnnotation(ClassName("dagger", "Binds"))
                    .addParameter(
                        paramName,
                        ClassName(paramType.substringBeforeLast("."), paramType.substringAfterLast("."))
                    )
                    .returns(ClassName(returnType.substringBeforeLast("."), returnType.substringAfterLast(".")))
                    .build()
            )
        }

        bindings.forEach { binding ->
            val iface = ClassName(
                binding.ifaceImport.removePrefix("import ").substringBeforeLast("."),
                binding.ifaceImport.substringAfterLast(".")
            )
            val impl = ClassName(
                binding.implImport.removePrefix("import ").substringBeforeLast("."),
                binding.implImport.substringAfterLast(".")
            )
            if (!existingFunctions.any { it.contains("bind${iface.simpleName}") }) {
                classBuilder.addFunction(
                    FunSpec.builder("bind${iface.simpleName}")
                        .addModifiers(KModifier.ABSTRACT)
                        .addAnnotation(ClassName("dagger", "Binds"))
                        .addParameter("dataSourceImpl", impl)
                        .returns(iface)
                        .build()
                )
            }
        }

        return FileSpec.builder(packageName, fileName)
            .addType(classBuilder.build())
            .build()
    }
}
