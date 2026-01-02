package com.jkjamies.cammp.feature.repositorygenerator.data.factory

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.squareup.kotlinpoet.*
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
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
        if (params.diStrategy is DiStrategy.Hilt) {
            constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
        }

        val baseName = stripRepositorySuffix(params.className)
        val dataBasePkg = dataPackage.substringBeforeLast(".repository")

        val generatedFqns = buildList {
            if (params.includeDatasource) {
                if (params.datasourceCombined) {
                    add("$dataBasePkg.dataSource.${baseName}DataSource")
                } else {
                    if (params.datasourceRemote) add("$dataBasePkg.remoteDataSource.${baseName}RemoteDataSource")
                    if (params.datasourceLocal) add("$dataBasePkg.localDataSource.${baseName}LocalDataSource")
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

    private fun stripRepositorySuffix(name: String): String =
        if (name.endsWith("Repository") && name.length > "Repository".length) name.removeSuffix("Repository") else name
}
