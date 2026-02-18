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

package com.jkjamies.cammp.feature.repositorygenerator.data.factory

import com.jkjamies.cammp.domain.codegen.GeneratedAnnotations
import com.jkjamies.cammp.domain.model.DiStrategy
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface DataSourceSpecFactory {
    fun createInterface(packageName: String, className: String): FileSpec

    fun createImplementation(
        packageName: String,
        className: String,
        interfacePackage: String,
        interfaceName: String,
        diStrategy: DiStrategy
    ): FileSpec
}

@ContributesBinding(AppScope::class)
internal class DataSourceSpecFactoryImpl : DataSourceSpecFactory {
    override fun createInterface(packageName: String, className: String): FileSpec {
        return FileSpec.builder(packageName, className)
            .addType(TypeSpec.interfaceBuilder(className).build())
            .build()
    }

    override fun createImplementation(
        packageName: String,
        className: String,
        interfacePackage: String,
        interfaceName: String,
        diStrategy: DiStrategy
    ): FileSpec {
        val ifaceClassName = ClassName(interfacePackage, interfaceName)
        val classBuilder = TypeSpec.classBuilder(className)
            .addSuperinterface(ifaceClassName)
        val constructorBuilder = FunSpec.constructorBuilder()

        when (diStrategy) {
            is DiStrategy.Metro -> {
                // @ContributesBinding implies @Inject, no explicit @Inject needed
                classBuilder.addAnnotation(
                    AnnotationSpec.builder(GeneratedAnnotations.METRO_CONTRIBUTES_BINDING)
                        .addMember("%T::class", GeneratedAnnotations.METRO_APP_SCOPE)
                        .build()
                )
            }
            is DiStrategy.Hilt -> {
                constructorBuilder.addAnnotation(GeneratedAnnotations.JAVAX_INJECT)
            }
            is DiStrategy.Koin -> Unit
        }

        classBuilder.primaryConstructor(constructorBuilder.build())

        return FileSpec.builder(packageName, className)
            .addType(classBuilder.build())
            .build()
    }
}