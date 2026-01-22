package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface IntentSpecFactory {
    fun create(packageName: String, params: PresentationParams): FileSpec
}

@ContributesBinding(AppScope::class)
class IntentSpecFactoryImpl : IntentSpecFactory {

    override fun create(packageName: String, params: PresentationParams): FileSpec {
        val intentName = "${params.screenName}Intent"
        val sealedInterface = TypeSpec.interfaceBuilder(intentName)
            .addModifiers(KModifier.INTERNAL, KModifier.SEALED)
            .addType(
                TypeSpec.objectBuilder("NoOp")
                    .addSuperinterface(ClassName(packageName, intentName))
                    .build()
            )
            .build()

        return FileSpec.builder(packageName, intentName)
            .addType(sealedInterface)
            .build()
    }
}
