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

package com.jkjamies.cammp.feature.usecasegenerator.datasource

import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.DiModuleDataSource
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

@ContributesBinding(AppScope::class)
class DiModuleDataSourceImpl : DiModuleDataSource {
    override fun generateKoinModuleContent(
        existingContent: String?,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        repositoryFqns: List<String>,
        useCaseInterfaceFqn: String?
    ): String {
        val useCaseClassName = ClassName(useCaseFqn.substringBeforeLast('.'), useCaseSimpleName)
        val repositoryClassNames = repositoryFqns.map { fqn ->
            ClassName(fqn.substringBeforeLast('.'), fqn.substringAfterLast('.'))
        }

        val newBinding = if (useCaseInterfaceFqn != null) {
            val interfacePkg = useCaseInterfaceFqn.substringBeforeLast('.')
            val interfaceName = useCaseInterfaceFqn.substringAfterLast('.')
            val interfaceClassName = ClassName(interfacePkg, interfaceName)
            CodeBlock.builder()
                .add("single<%T> { %T(", interfaceClassName, useCaseClassName)
                .apply {
                    if (repositoryClassNames.isNotEmpty()) {
                        add(repositoryClassNames.joinToString(", ") { "get()" })
                    }
                }
                .add(") }")
                .build()
        } else {
            CodeBlock.builder()
                .add("single { %T(", useCaseClassName)
                .apply {
                    if (repositoryClassNames.isNotEmpty()) {
                        add(repositoryClassNames.joinToString(", ") { "get()" })
                    }
                }
                .add(") }")
                .build()
        }

        val fileSpecBuilder = FileSpec.builder(diPackage, "UseCaseModule")
            .addImport("org.koin.dsl", "module")

        val moduleBlockBuilder = CodeBlock.builder().beginControlFlow("module")

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

        return fileSpec.toString()
    }

    override fun generateHiltModuleContent(
        existingContent: String?,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        useCaseInterfaceFqn: String
    ): String {
        val useCaseClassName = ClassName(useCaseFqn.substringBeforeLast('.'), useCaseSimpleName)
        val interfacePkg = useCaseInterfaceFqn.substringBeforeLast('.')
        val interfaceName = useCaseInterfaceFqn.substringAfterLast('.')
        val interfaceClassName = ClassName(interfacePkg, interfaceName)

        val bindFunName = "binds$useCaseSimpleName"
        val bindFun = FunSpec.builder(bindFunName)
            .addAnnotation(ClassName("dagger", "Binds"))
            .addModifiers(KModifier.ABSTRACT)
            .addParameter("impl", useCaseClassName)
            .returns(interfaceClassName)
            .build()

        val moduleBuilder = TypeSpec.classBuilder("UseCaseModule")
            .addModifiers(KModifier.ABSTRACT)
            .addAnnotation(ClassName("dagger", "Module"))
            .addAnnotation(
                AnnotationSpec.builder(ClassName("dagger.hilt", "InstallIn"))
                    .addMember("%T::class", ClassName("dagger.hilt.components", "SingletonComponent"))
                    .build()
            )
            .addFunction(bindFun)

        val fileSpecBuilder = FileSpec.builder(diPackage, "UseCaseModule")

        if (existingContent != null) {
            // Very basic merging for Hilt - just add the new function to the existing class if it's there
            // For now, let's just recreate it if it doesn't have the function. 
            // Proper merging would require parsing the existing TypeSpec.
            if (existingContent.contains(bindFunName)) {
                return existingContent
            }
            
            // Fallback: overwrite for now or handle more gracefully. 
            // Since this is a specialized UseCaseModule, overwriting with accumulated binds is better but hard without a real parser.
            // Let's at least keep the existing imports.
            existingContent.lineSequence()
                .filter { it.trim().startsWith("import ") }
                .map { it.trim().removePrefix("import ").trim() }
                .forEach { importLine ->
                    val pkg = importLine.substringBeforeLast('.', "")
                    val name = importLine.substringAfterLast('.')
                    if (pkg.isNotEmpty()) {
                        fileSpecBuilder.addImport(pkg, name)
                    }
                }
        }

        return fileSpecBuilder
            .addType(moduleBuilder.build())
            .build()
            .toString()
    }
}