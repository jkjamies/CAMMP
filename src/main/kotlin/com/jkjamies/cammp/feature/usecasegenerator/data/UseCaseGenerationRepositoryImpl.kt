package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import com.squareup.kotlinpoet.*
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class UseCaseGenerationRepositoryImpl(
    private val modulePkgRepo: ModulePackageRepository = ModulePackageRepositoryImpl()
) : UseCaseGenerationRepository {

    override fun generateUseCase(params: UseCaseParams, packageName: String): Path {
        val fileSpec = createFileSpec(packageName, params)
        val content = fileSpec.toString()
            .replace("import org.koin.core.`annotation`.Single", "import org.koin.core.annotation.Single")
        val targetDir = params.domainDir.resolve("src/main/kotlin").resolve(packageName.replace('.', '/'))
        targetDir.createDirectories()
        val out = targetDir.resolve("${params.className}.kt")
        out.writeText(content)
        return out
    }

    private fun createFileSpec(packageName: String, p: UseCaseParams): FileSpec {
        val useCaseClassName = ClassName(packageName, p.className)
        val classBuilder = TypeSpec.classBuilder(useCaseClassName)

        if (p.useKoin && p.koinAnnotations) {
            classBuilder.addAnnotation(AnnotationSpec.builder(ClassName("org.koin.core.annotation", "Single")).build())
        }

        val constructorBuilder = FunSpec.constructorBuilder()
        if (!p.useKoin) {
            constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
        }

        val existingPkg = modulePkgRepo.findModulePackage(p.domainDir)
            ?: error("Could not determine existing package for selected domain module")
        val marker = ".domain"
        val idx = existingPkg.lastIndexOf(marker)
        val baseDomain = if (idx >= 0) existingPkg.substring(0, idx + marker.length) else existingPkg

        p.repositories.forEach { repo ->
            val repoClassName = ClassName("$baseDomain.repository", repo)
            val propertyName = repo.replaceFirstChar { it.lowercase() }
            constructorBuilder.addParameter(propertyName, repoClassName)
            classBuilder.addProperty(
                PropertySpec.builder(propertyName, repoClassName)
                    .initializer(propertyName)
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }
        classBuilder.primaryConstructor(constructorBuilder.build())

        val invokeFun = FunSpec.builder("invoke")
            .addModifiers(KModifier.SUSPEND, KModifier.OPERATOR)
            .build()
        classBuilder.addFunction(invokeFun)

        return FileSpec.builder(packageName, p.className)
            .addType(classBuilder.build())
            .build()
    }
}
