package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface ScreenSpecFactory {
    fun create(
        packageName: String,
        screenName: String,
        diHilt: Boolean,
        diKoin: Boolean
    ): FileSpec
}

@ContributesBinding(AppScope::class)
@Inject
class ScreenSpecFactoryImpl : ScreenSpecFactory {

    override fun create(
        packageName: String,
        screenName: String,
        diHilt: Boolean,
        diKoin: Boolean
    ): FileSpec {
        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val modifierClass = ClassName("androidx.compose.ui", "Modifier")
        val viewModelName = "${screenName}ViewModel"
        val viewModelClass = ClassName(packageName, viewModelName)

        val screenFunc = FunSpec.builder(screenName)
            .addAnnotation(composableAnnotation)
            .addParameter("modifier", modifierClass.copy(nullable = true))
            .addCode("val viewModel: %T = ", viewModelClass)

        if (diHilt) {
            val hiltViewModel = ClassName("androidx.hilt.navigation.compose", "hiltViewModel")
            screenFunc.addCode("%M()", hiltViewModel)
        } else if (diKoin) {
            val koinViewModel = ClassName("org.koin.androidx.compose", "koinViewModel")
            screenFunc.addCode("%M()", koinViewModel)
        } else {
            // Fallback or manual instantiation if needed, but for now assuming DI
            screenFunc.addCode("TODO(\"Provide ViewModel\")")
        }

        return FileSpec.builder(packageName, screenName)
            .addFunction(screenFunc.build())
            .build()
    }
}
