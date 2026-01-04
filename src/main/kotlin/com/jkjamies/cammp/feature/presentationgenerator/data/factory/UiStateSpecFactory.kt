package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface UiStateSpecFactory {
    fun create(packageName: String, screenName: String): FileSpec
}

@ContributesBinding(AppScope::class)
@Inject
class UiStateSpecFactoryImpl : UiStateSpecFactory {

    override fun create(packageName: String, screenName: String): FileSpec {
        val uiStateName = "${screenName}UiState"
        val classBuilder = TypeSpec.classBuilder(uiStateName)
            .addModifiers(KModifier.INTERNAL, KModifier.DATA)
            .primaryConstructor(
                com.squareup.kotlinpoet.FunSpec.constructorBuilder()
                    .addParameter(
                        com.squareup.kotlinpoet.ParameterSpec.builder("isLoading", Boolean::class)
                            .defaultValue("false")
                            .build()
                    )
                    .build()
            )
            .addProperty(
                com.squareup.kotlinpoet.PropertySpec.builder("isLoading", Boolean::class)
                    .initializer("isLoading")
                    .build()
            )

        return FileSpec.builder(packageName, uiStateName)
            .addType(classBuilder.build())
            .build()
    }
}
