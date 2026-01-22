package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
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
class ScreenSpecFactoryImpl : ScreenSpecFactory {

    override fun create(
        packageName: String,
        screenName: String,
        diHilt: Boolean,
        diKoin: Boolean
    ): FileSpec {
        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val viewModelName = "${screenName}ViewModel"
        val viewModelClass = ClassName(packageName, viewModelName)

        val screenFunc = FunSpec.builder(screenName)
            .addAnnotation(composableAnnotation)
            .addModifiers(KModifier.INTERNAL)

        when {
            diHilt -> {
                val hiltViewModel = MemberName("androidx.hilt.lifecycle.viewmodel.compose", "hiltViewModel")
                screenFunc.addParameter(
                    ParameterSpec.builder("viewModel", viewModelClass)
                        .defaultValue("%M()", hiltViewModel)
                        .build()
                )
            }

            diKoin -> {
                val koinViewModel = MemberName("org.koin.androidx.compose", "koinViewModel")
                screenFunc.addParameter(
                    ParameterSpec.builder("viewModel", viewModelClass)
                        .defaultValue("%M()", koinViewModel)
                        .build()
                )
            }

            else -> {
                // Keep compilation valid without DI configured.
                screenFunc.addParameter("viewModel", viewModelClass)
            }
        }

        return FileSpec.builder(packageName, screenName)
            .addFunction(screenFunc.build())
            .build()
    }
}
