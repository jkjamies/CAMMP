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

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
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

interface RepositorySpecFactory {
    fun createDomainInterface(packageName: String, params: RepositoryParams): FileSpec
    fun createDataImplementation(dataPackage: String, domainPackage: String, params: RepositoryParams): FileSpec
}

@ContributesBinding(AppScope::class)
class RepositorySpecFactoryImpl : RepositorySpecFactory {

    override fun createDomainInterface(packageName: String, params: RepositoryParams): FileSpec {
        return FileSpec.builder(packageName, params.className)
            .addType(
                TypeSpec.interfaceBuilder(params.className)
                    .build()
            )
            .build()
    }

    override fun createDataImplementation(dataPackage: String, domainPackage: String, params: RepositoryParams): FileSpec {
        val domainClassName = ClassName(domainPackage, params.className)
        val implClassName = "${params.className}Impl"
        val classBuilder = TypeSpec.classBuilder(implClassName)
            .addSuperinterface(domainClassName)

        if (params.diStrategy is DiStrategy.Koin && params.diStrategy.useAnnotations) {
            classBuilder.addAnnotation(AnnotationSpec.builder(ClassName("org.koin.core.annotation", "Single")).build())
        }

        val constructorBuilder = FunSpec.constructorBuilder()
        if (params.diStrategy is DiStrategy.Hilt || params.diStrategy is DiStrategy.Metro) {
            constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
        }

        val baseName = params.className
        val dataBasePkg = dataPackage.substringBeforeLast(".repository")

        val generatedFqns = buildList {
            when (params.datasourceStrategy) {
                DatasourceStrategy.None -> Unit
                DatasourceStrategy.Combined -> add("$dataBasePkg.dataSource.${baseName}DataSource")
                DatasourceStrategy.RemoteOnly -> add("$dataBasePkg.remoteDataSource.${baseName}RemoteDataSource")
                DatasourceStrategy.LocalOnly -> add("$dataBasePkg.localDataSource.${baseName}LocalDataSource")
                DatasourceStrategy.RemoteAndLocal -> {
                    add("$dataBasePkg.remoteDataSource.${baseName}RemoteDataSource")
                    add("$dataBasePkg.localDataSource.${baseName}LocalDataSource")
                }
            }
        }
        val allFqns: List<String> = (params.selectedDataSources + generatedFqns).distinct()

        allFqns.forEach { fqn ->
            val simpleName = fqn.substringAfterLast('.')
            val paramName = simpleName.replaceFirstChar { it.lowercase() }
            val className = ClassName(fqn.substringBeforeLast('.'), simpleName)
            constructorBuilder.addParameter(paramName, className)
            classBuilder.addProperty(
                PropertySpec.builder(paramName, className)
                    .initializer(paramName)
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }

        classBuilder.primaryConstructor(constructorBuilder.build())

        return FileSpec.builder(dataPackage, implClassName)
            .addType(classBuilder.build())
            .build()
    }
}