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
@Inject
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