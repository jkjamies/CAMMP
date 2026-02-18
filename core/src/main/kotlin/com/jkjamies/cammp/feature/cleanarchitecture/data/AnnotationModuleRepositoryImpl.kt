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

package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.domain.codegen.GeneratedAnnotations
import com.jkjamies.cammp.domain.codegen.toCleanString
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AnnotationModuleRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

@ContributesBinding(AppScope::class)
class AnnotationModuleRepositoryImpl(
    private val fs: FileSystemRepository
) : AnnotationModuleRepository {

    override fun generate(
        outputDirectory: Path,
        packageName: String,
        scanPackage: String,
        featureName: String
    ) {
        val featureTitleCase = featureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val className = "${featureTitleCase}AnnotationsModule"

        val moduleAnnotation = AnnotationSpec.builder(GeneratedAnnotations.KOIN_ANNOTATION_MODULE)
            .build()

        val componentScanAnnotation = AnnotationSpec.builder(GeneratedAnnotations.KOIN_COMPONENT_SCAN)
            .addMember("%S", scanPackage)
            .build()

        val typeSpec = TypeSpec.classBuilder(className)
            .addAnnotation(moduleAnnotation)
            .addAnnotation(componentScanAnnotation)
            .build()

        val fileSpec = FileSpec.builder(packageName, className)
            .addType(typeSpec)
            .build()

        val outputFile = outputDirectory.resolve("$className.kt")
        fs.writeText(outputFile, fileSpec.toCleanString())
    }
}