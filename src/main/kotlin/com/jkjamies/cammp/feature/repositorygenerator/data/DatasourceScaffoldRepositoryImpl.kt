package com.jkjamies.cammp.feature.repositorygenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
@Inject
class DatasourceScaffoldRepositoryImpl : DatasourceScaffoldRepository {

    override fun generateInterface(
        directory: Path,
        packageName: String,
        className: String
    ): Path {
        directory.createDirectories()
        val fileSpec = FileSpec.builder(packageName, className)
            .addType(TypeSpec.interfaceBuilder(className).build())
            .build()
        val outFile = directory.resolve("$className.kt")
        outFile.writeText(fileSpec.toString().replace("`data`", "data"))
        return outFile
    }

    override fun generateImplementation(
        directory: Path,
        packageName: String,
        className: String,
        interfacePackage: String,
        interfaceName: String,
        useKoin: Boolean
    ): Path {
        directory.createDirectories()
        val ifaceClassName = ClassName(interfacePackage, interfaceName)
        val implClassName = className
        
        val constructorBuilder = FunSpec.constructorBuilder()
        if (!useKoin) {
            constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
        }
        
        val fileSpec = FileSpec.builder(packageName, implClassName)
            .addType(
                TypeSpec.classBuilder(implClassName)
                    .addSuperinterface(ifaceClassName)
                    .primaryConstructor(constructorBuilder.build())
                    .build()
            )
            .build()
            
        val outFile = directory.resolve("$implClassName.kt")
        outFile.writeText(fileSpec.toString().replace("`data`", "data"))
        return outFile
    }
}
