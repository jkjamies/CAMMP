package com.jkjamies.cammp.feature.repositorygenerator.data.factory

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
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
@Inject
class RepositorySpecFactoryImpl : RepositorySpecFactory {
    override fun createDomainInterface(packageName: String, params: RepositoryParams): FileSpec {
        return FileSpec.builder(packageName, params.className)
            .addType(TypeSpec.interfaceBuilder(params.className).build())
            .build()
    }

    override fun createDataImplementation(
        dataPackage: String,
        domainPackage: String,
        params: RepositoryParams
    ): FileSpec {
        val domainInterface = ClassName(domainPackage, params.className)
        val implName = "${params.className}Impl"
        val constructorBuilder = FunSpec.constructorBuilder()

        if (params.diStrategy is DiStrategy.Hilt) {
            constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
        }

        val classBuilder = TypeSpec.classBuilder(implName)
            .addSuperinterface(domainInterface)

        params.selectedDataSources.forEach { fqn ->
            val simpleName = fqn.substringAfterLast('.')
            val pkg = fqn.substringBeforeLast('.')
            val typeName = ClassName(pkg, simpleName)
            val propName = simpleName.replaceFirstChar { it.lowercase() }
            constructorBuilder.addParameter(propName, typeName)
            classBuilder.addProperty(
                PropertySpec.builder(propName, typeName)
                    .initializer(propName)
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }

        classBuilder.primaryConstructor(constructorBuilder.build())

        return FileSpec.builder(dataPackage, implName)
            .addType(classBuilder.build())
            .build()
    }
}