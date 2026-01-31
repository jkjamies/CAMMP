/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkjamies.cammp.feature.repositorygenerator.datasource

import com.jkjamies.cammp.feature.repositorygenerator.data.datasource.RepoDiModuleDataSource
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.collections.forEach

@ContributesBinding(AppScope::class)
class RepoDiModuleDataSourceImpl : RepoDiModuleDataSource {

    override fun generateKoinModule(
        packageName: String,
        existingContent: String?,
        domainFqn: String,
        dataFqn: String,
        className: String
    ): String {
        val domainClassName = ClassName(domainFqn.substringBeforeLast('.'), domainFqn.substringAfterLast('.'))
        val dataClassName = ClassName(dataFqn.substringBeforeLast('.'), dataFqn.substringAfterLast('.'))

        val fileSpec = createKoinFileSpec(packageName, "RepositoryModule", "repositoryModule", existingContent) { moduleBlock ->
            moduleBlock.addStatement("single<%T> { %T(get()) }", domainClassName, dataClassName)
        }
        return fileSpec.toString()
    }

    override fun generateHiltModule(
        packageName: String,
        existingContent: String?,
        domainFqn: String,
        dataFqn: String,
        className: String
    ): String {
        val domainClassName = ClassName(domainFqn.substringBeforeLast('.'), domainFqn.substringAfterLast('.'))
        val dataClassName = ClassName(dataFqn.substringBeforeLast('.'), dataFqn.substringAfterLast('.'))

        val fileSpec = createHiltFileSpec(packageName, "RepositoryModule", existingContent) { classBuilder, existingFunctions ->
            val bindingFunctionName = "bind$className"
            if (!existingFunctions.any { it.contains(bindingFunctionName) }) {
                classBuilder.addFunction(
                    FunSpec.builder(bindingFunctionName)
                        .addModifiers(KModifier.ABSTRACT)
                        .addAnnotation(ClassName("dagger", "Binds"))
                        .addParameter("repositoryImpl", dataClassName)
                        .returns(domainClassName)
                        .build()
                )
            }
        }
        return fileSpec.toString()
    }

    override fun generateKoinDataSourceModule(
        packageName: String,
        existingContent: String?,
        bindings: List<DataSourceBinding>
    ): String {
        val fileSpec = createKoinFileSpec(packageName, "DataSourceModule", "dataSourceModule", existingContent) { moduleBlock ->
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
        }
        return fileSpec.toString()
    }

    override fun generateHiltDataSourceModule(
        packageName: String,
        existingContent: String?,
        bindings: List<DataSourceBinding>
    ): String {
        val fileSpec = createHiltFileSpec(packageName, "DataSourceModule", existingContent) { classBuilder, existingFunctions ->
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
        }
        return fileSpec.toString()
    }

    private fun createKoinFileSpec(
        packageName: String,
        fileName: String,
        propertyName: String,
        existingContent: String?,
        addBindings: (CodeBlock.Builder) -> Unit
    ): FileSpec {
        val moduleBlock = CodeBlock.builder()
        val existingImports = parseImports(existingContent)

        if (existingContent != null) {
            val body = existingContent.substringAfter("module {").substringBeforeLast("}")
            if (body.isNotBlank()) {
                moduleBlock.add(body.trimIndent())
                moduleBlock.add("\n")
            }
        }

        addBindings(moduleBlock)

        val fileSpecBuilder = FileSpec.builder(packageName, fileName)
            .addImport("org.koin.dsl", "module")
            .addImport("org.koin.core.module", "Module")

        addExistingImports(fileSpecBuilder, existingImports, packageName)

        return fileSpecBuilder
            .addProperty(
                PropertySpec.builder(propertyName, ClassName("org.koin.core.module", "Module"))
                    .initializer(
                        CodeBlock.builder().beginControlFlow("module").add(moduleBlock.build()).endControlFlow().build()
                    )
                    .build()
            )
            .build()
    }

    private fun createHiltFileSpec(
        packageName: String,
        fileName: String,
        existingContent: String?,
        addBindings: (TypeSpec.Builder, Set<String>) -> Unit
    ): FileSpec {
        val classBuilder = TypeSpec.classBuilder(fileName)
            .addModifiers(KModifier.ABSTRACT)
            .addAnnotation(ClassName("dagger", "Module"))
            .addAnnotation(
                AnnotationSpec.builder(ClassName("dagger.hilt", "InstallIn"))
                    .addMember("%T::class", ClassName("dagger.hilt.components", "SingletonComponent"))
                    .build()
            )

        val imports = parseImportsMap(existingContent)
        val existingFunctions = parseExistingHiltFunctions(existingContent, fileName)

        existingFunctions.forEach {
            addExistingHiltFunction(classBuilder, it, imports, packageName)
        }

        addBindings(classBuilder, existingFunctions)

        return FileSpec.builder(packageName, fileName)
            .addType(classBuilder.build())
            .build()
    }

    private fun parseImports(content: String?): Set<String> {
        val imports = mutableSetOf<String>()
        content?.lines()?.forEach { line ->
            if (line.trim().startsWith("import ")) {
                imports.add(line.trim().removePrefix("import ").trim())
            }
        }
        return imports
    }

    private fun parseImportsMap(content: String?): Map<String, String> {
        val imports = mutableMapOf<String, String>()
        content?.lines()?.forEach { line ->
            if (line.trim().startsWith("import ")) {
                val fqn = line.trim().removePrefix("import ").trim()
                val simpleName = fqn.substringAfterLast(".")
                imports[simpleName] = fqn
            }
        }
        return imports
    }

    private fun addExistingImports(fileSpecBuilder: FileSpec.Builder, imports: Set<String>, packageName: String) {
        imports.forEach { import ->
            if (import.contains(".")) {
                val pkg = import.substringBeforeLast(".")
                val name = import.substringAfterLast(".")
                if (pkg != packageName && pkg != "org.koin.dsl" && pkg != "org.koin.core.module") {
                    fileSpecBuilder.addImport(pkg, name)
                }
            }
        }
    }

    private fun parseExistingHiltFunctions(content: String?, fileName: String): Set<String> {
        val existingFunctions = mutableSetOf<String>()
        if (content != null) {
            val body = content.substringAfter("abstract class $fileName {").substringBeforeLast("}")
            if (body.isNotBlank()) {
                body.lines().forEach { line ->
                    if (line.contains("fun bind")) {
                        existingFunctions.add(line.trim())
                    }
                }
            }
        }
        return existingFunctions
    }

    private fun addExistingHiltFunction(
        classBuilder: TypeSpec.Builder,
        functionSignature: String,
        imports: Map<String, String>,
        packageName: String
    ) {
        val funName = functionSignature.substringAfter("fun ").substringBefore("(")
        val param = functionSignature.substringAfter("(").substringBefore(")")
        val paramName = param.substringBefore(":").trim()
        val paramType = param.substringAfter(":").trim()
        val returnType = functionSignature.substringAfter("):").trim()

        val paramTypeSimple = paramType.substringAfterLast(".")
        val paramTypeFqn = imports[paramTypeSimple] ?: paramType
        val paramClassName = if (paramTypeFqn.contains(".")) {
            ClassName(paramTypeFqn.substringBeforeLast("."), paramTypeFqn.substringAfterLast("."))
        } else {
            ClassName(packageName, paramTypeFqn)
        }

        val returnTypeSimple = returnType.substringAfterLast(".")
        val returnTypeFqn = imports[returnTypeSimple] ?: returnType
        val returnClassName = if (returnTypeFqn.contains(".")) {
            ClassName(returnTypeFqn.substringBeforeLast("."), returnTypeFqn.substringAfterLast("."))
        } else {
            ClassName(packageName, returnTypeFqn)
        }

        classBuilder.addFunction(
            FunSpec.builder(funName)
                .addModifiers(KModifier.ABSTRACT)
                .addAnnotation(ClassName("dagger", "Binds"))
                .addParameter(
                    paramName,
                    paramClassName
                )
                .returns(returnClassName)
                .build()
        )
    }
}
