package com.jkjamies.cammp.feature.usecasegenerator.data.factory

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface UseCaseSpecFactory {
    fun create(
        packageName: String,
        params: UseCaseParams,
        baseDomainPackage: String,
        interfaceFqn: String? = null
    ): FileSpec

    fun createInterface(packageName: String, className: String): FileSpec
}

@ContributesBinding(AppScope::class)
class UseCaseSpecFactoryImpl : UseCaseSpecFactory {
    override fun create(
        packageName: String,
        params: UseCaseParams,
        baseDomainPackage: String,
        interfaceFqn: String?
    ): FileSpec {
        val className = params.className
        val classBuilder = TypeSpec.classBuilder(className)

        if (interfaceFqn != null) {
            val interfacePkg = interfaceFqn.substringBeforeLast(".")
            val interfaceName = interfaceFqn.substringAfterLast(".")
            classBuilder.addSuperinterface(ClassName(interfacePkg, interfaceName))
        }

        val constructorBuilder = FunSpec.constructorBuilder()

        when (val di = params.diStrategy) {
            is DiStrategy.Metro,
            is DiStrategy.Hilt -> {
                constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
            }
            is DiStrategy.Koin -> {
                if (di.useAnnotations && interfaceFqn == null) {
                    classBuilder.addAnnotation(ClassName("org.koin.core.annotation", "Single"))
                }
            }
        }

        params.repositories.forEach { repoName ->
            val repoClass = ClassName("$baseDomainPackage.repository", repoName)
            val paramName = repoName.replaceFirstChar { it.lowercase() }
            constructorBuilder.addParameter(paramName, repoClass)
            classBuilder.addProperty(
                PropertySpec.builder(paramName, repoClass)
                    .initializer(paramName)
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }

        if (params.repositories.isNotEmpty() || params.diStrategy == DiStrategy.Hilt || params.diStrategy == DiStrategy.Metro) {
            classBuilder.primaryConstructor(constructorBuilder.build())
        }

        classBuilder.addFunction(
            FunSpec.builder("invoke")
                .addModifiers(KModifier.SUSPEND, KModifier.OPERATOR)
                .apply {
                    if (interfaceFqn != null) {
                        addModifiers(KModifier.OVERRIDE)
                    }
                }
                .build()
        )

        return FileSpec.builder(packageName, className)
            .addType(classBuilder.build())
            .build()
    }

    override fun createInterface(packageName: String, className: String): FileSpec {
        val classBuilder = TypeSpec.interfaceBuilder(className)
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.SUSPEND, KModifier.ABSTRACT, KModifier.OPERATOR)
                    .build()
            )

        return FileSpec.builder(packageName, className)
            .addType(classBuilder.build())
            .build()
    }
}