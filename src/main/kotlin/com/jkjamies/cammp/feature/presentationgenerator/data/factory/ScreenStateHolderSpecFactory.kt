package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface ScreenStateHolderSpecFactory {
    fun create(packageName: String, screenName: String): FileSpec
}

@ContributesBinding(AppScope::class)
@Inject
class ScreenStateHolderSpecFactoryImpl : ScreenStateHolderSpecFactory {

    override fun create(packageName: String, screenName: String): FileSpec {
        val stateHolderName = "${screenName}StateHolder"
        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val remember = MemberName("androidx.compose.runtime", "remember")

        val classBuilder = TypeSpec.classBuilder(stateHolderName)
            .addModifiers(KModifier.INTERNAL)

        val rememberFunc = FunSpec.builder("remember${screenName}State")
            .addAnnotation(composableAnnotation)
            .addModifiers(KModifier.INTERNAL)
            .returns(ClassName(packageName, stateHolderName))
            .addStatement("return %M { %T() }", remember, ClassName(packageName, stateHolderName))
            .build()

        return FileSpec.builder(packageName, stateHolderName)
            .addType(classBuilder.build())
            .addFunction(rememberFunc)
            .build()
    }
}
