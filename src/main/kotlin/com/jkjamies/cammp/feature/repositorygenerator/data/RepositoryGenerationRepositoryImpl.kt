package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class RepositoryGenerationRepositoryImpl : RepositoryGenerationRepository {

    override fun generateDomainLayer(params: RepositoryParams, packageName: String, domainDir: Path): Path {
        val fileSpec = createDomainFileSpec(packageName, params)
        val targetDir = domainDir.resolve("src/main/kotlin").resolve(packageName.replace('.', '/'))
        targetDir.createDirectories()
        val out = targetDir.resolve("${params.className}.kt")
        out.writeText(fileSpec.toString().replace("`data`", "data"))
        return out
    }

    override fun generateDataLayer(params: RepositoryParams, dataPackage: String, domainPackage: String): Path {
        val fileSpec = createDataFileSpec(dataPackage, domainPackage, params)
        val targetDir = params.dataDir.resolve("src/main/kotlin").resolve(dataPackage.replace('.', '/'))
        targetDir.createDirectories()
        val out = targetDir.resolve("${params.className}Impl.kt")
        out.writeText(
            fileSpec.toString().replace("`data`", "data")
                .replace("import org.koin.core.`annotation`.Single", "import org.koin.core.annotation.Single")
        )
        return out
    }

    private fun createDomainFileSpec(domainPackage: String, p: RepositoryParams): FileSpec {
        return FileSpec.builder(domainPackage, p.className)
            .addType(
                TypeSpec.interfaceBuilder(p.className)
                    .build()
            )
            .build()
    }

    private fun createDataFileSpec(dataPackage: String, domainPackage: String, p: RepositoryParams): FileSpec {
        val domainClassName = ClassName(domainPackage, p.className)
        val implClassName = "${p.className}Impl"
        val classBuilder = TypeSpec.classBuilder(implClassName)
            .addSuperinterface(domainClassName)

        if (p.useKoin && p.koinAnnotations) {
            classBuilder.addAnnotation(AnnotationSpec.builder(ClassName("org.koin.core.annotation", "Single")).build())
        }

        val constructorBuilder = FunSpec.constructorBuilder()
        if (!p.useKoin) {
            constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
        }

        val baseName = stripRepositorySuffix(p.className)
        val dataBasePkg = dataPackage.substringBeforeLast(".repository")

        val generatedFqns = buildList {
            if (p.includeDatasource) {
                if (p.datasourceCombined) {
                    add("$dataBasePkg.dataSource.${baseName}DataSource")
                } else {
                    if (p.datasourceRemote) add("$dataBasePkg.remoteDataSource.${baseName}RemoteDataSource")
                    if (p.datasourceLocal) add("$dataBasePkg.localDataSource.${baseName}LocalDataSource")
                }
            }
        }
        val allFqns: List<String> = (p.selectedDataSources + generatedFqns).distinct()

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

    private fun stripRepositorySuffix(name: String): String =
        if (name.endsWith("Repository") && name.length > "Repository".length) name.removeSuffix("Repository") else name
}
