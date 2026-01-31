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
        useKoin: Boolean
    ): FileSpec
}

@ContributesBinding(AppScope::class)
class DataSourceSpecFactoryImpl : DataSourceSpecFactory {
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
        useKoin: Boolean
    ): FileSpec {
        val ifaceClassName = ClassName(interfacePackage, interfaceName)
        val constructorBuilder = FunSpec.constructorBuilder()
        if (!useKoin) {
            constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
        }

        return FileSpec.builder(packageName, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .addSuperinterface(ifaceClassName)
                    .primaryConstructor(constructorBuilder.build())
                    .build()
            )
            .build()
    }
}