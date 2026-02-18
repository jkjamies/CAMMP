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

package com.jkjamies.cammp.feature.usecasegenerator.data.factory

import com.jkjamies.cammp.domain.codegen.GeneratedAnnotations
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface UseCaseSpecFactory {
    fun create(
        packageName: String,
        params: UseCaseParams,
        baseDomainPackage: String,
        interfaceFqn: String? = null
    ): FileSpec

    fun createInterface(packageName: String, className: String): FileSpec
}

@ContributesBinding(AppScope::class)
internal class UseCaseSpecFactoryImpl : UseCaseSpecFactory {
    override fun create(
        packageName: String,
        params: UseCaseParams,
        baseDomainPackage: String,
        interfaceFqn: String?
    ): FileSpec {
        val className = params.className
        val classBuilder = TypeSpec.classBuilder(className)

        if (interfaceFqn != null) {
            val interfacePkg = interfaceFqn.substringBeforeLast(".")
            val interfaceName = interfaceFqn.substringAfterLast(".")
            classBuilder.addSuperinterface(ClassName(interfacePkg, interfaceName))
        }

        val constructorBuilder = FunSpec.constructorBuilder()

        when (val di = params.diStrategy) {
            is DiStrategy.Metro -> {
                if (interfaceFqn != null) {
                    // @ContributesBinding implies @Inject, no explicit @Inject needed
                    classBuilder.addAnnotation(
                        AnnotationSpec.builder(GeneratedAnnotations.METRO_CONTRIBUTES_BINDING)
                            .addMember("%T::class", GeneratedAnnotations.METRO_APP_SCOPE)
                            .build()
                    )
                } else {
                    // Standalone use case — explicit @Inject required
                    classBuilder.addAnnotation(GeneratedAnnotations.METRO_INJECT)
                }
            }
            is DiStrategy.Hilt -> {
                constructorBuilder.addAnnotation(GeneratedAnnotations.JAVAX_INJECT)
            }
            is DiStrategy.Koin -> {
                if (di.useAnnotations && interfaceFqn == null) {
                    classBuilder.addAnnotation(GeneratedAnnotations.KOIN_SINGLE)
                }
            }
        }

        params.repositories.forEach { repoName ->
            val repoClass = ClassName("$baseDomainPackage.repository", repoName)
            val paramName = repoName.replaceFirstChar { it.lowercase() }
            constructorBuilder.addParameter(paramName, repoClass)
            classBuilder.addProperty(
                PropertySpec.builder(paramName, repoClass)
                    .initializer(paramName)
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }

        if (params.repositories.isNotEmpty() || params.diStrategy == DiStrategy.Hilt || params.diStrategy == DiStrategy.Metro) {
            classBuilder.primaryConstructor(constructorBuilder.build())
        }

        classBuilder.addFunction(
            FunSpec.builder("invoke")
                .addModifiers(KModifier.SUSPEND, KModifier.OPERATOR)
                .apply {
                    if (interfaceFqn != null) {
                        addModifiers(KModifier.OVERRIDE)
                    }
                }
                .build()
        )

        return FileSpec.builder(packageName, className)
            .addType(classBuilder.build())
            .build()
    }

    override fun createInterface(packageName: String, className: String): FileSpec {
        val classBuilder = TypeSpec.interfaceBuilder(className)
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.SUSPEND, KModifier.ABSTRACT, KModifier.OPERATOR)
                    .build()
            )

        return FileSpec.builder(packageName, className)
            .addType(classBuilder.build())
            .build()
    }
}