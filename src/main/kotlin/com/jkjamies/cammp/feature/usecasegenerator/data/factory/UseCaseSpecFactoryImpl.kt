package com.jkjamies.cammp.feature.usecasegenerator.data.factory

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class UseCaseSpecFactoryImpl : UseCaseSpecFactory {
    override fun create(packageName: String, params: UseCaseParams, baseDomainPackage: String): FileSpec {
        val useCaseClassName = ClassName(packageName, params.className)
        val classBuilder = TypeSpec.classBuilder(useCaseClassName)

        if (params.diStrategy is DiStrategy.Koin && params.diStrategy.useAnnotations) {
            classBuilder.addAnnotation(AnnotationSpec.builder(ClassName("org.koin.core.annotation", "Single")).build())
        }

        val constructorBuilder = FunSpec.constructorBuilder()
        if (params.diStrategy is DiStrategy.Hilt) {
            constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
        }

        params.repositories.forEach { repo ->
            val repoClassName = ClassName("$baseDomainPackage.repository", repo)
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

        return FileSpec.builder(packageName, params.className)
            .addType(classBuilder.build())
            .build()
    }
}