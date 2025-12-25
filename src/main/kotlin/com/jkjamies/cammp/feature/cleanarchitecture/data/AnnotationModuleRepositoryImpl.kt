package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AnnotationModuleRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.nio.file.Path

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

        val moduleAnnotation = AnnotationSpec.builder(ClassName("org.koin.core.annotation", "Module"))
            .build()

        val componentScanAnnotation = AnnotationSpec.builder(ClassName("org.koin.core.annotation", "ComponentScan"))
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
        val content = fileSpec.toString()
            .replace("import org.koin.core.`annotation`.ComponentScan", "import org.koin.core.annotation.ComponentScan")
            .replace("import org.koin.core.`annotation`.Module", "import org.koin.core.annotation.Module")
            
        fs.writeText(outputFile, content)
    }
}
