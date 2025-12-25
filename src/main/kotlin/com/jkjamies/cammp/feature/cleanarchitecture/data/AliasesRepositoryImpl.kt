package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AliasesRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.nio.file.Path

class AliasesRepositoryImpl(
    private val fs: FileSystemRepository
) : AliasesRepository {

    override fun generateAliases(outputDirectory: Path, packageName: String, diMode: DiMode) {
        val aliasesObject = TypeSpec.objectBuilder("Aliases")
            .addModifiers(KModifier.INTERNAL)
            .addType(buildOperationsObject())
            .addType(buildPluginAliasesObject(diMode))
            .addType(buildBuildPropAliasesObject())
            .addType(buildDependenciesObject(diMode))
            .build()

        val fileSpec = FileSpec.builder(packageName, "Aliases")
            .addType(aliasesObject)
            .build()

        val outputFile = outputDirectory.resolve("Aliases.kt")
        fs.writeText(outputFile, fileSpec.toString())
    }

    private fun buildOperationsObject(): TypeSpec {
        return TypeSpec.objectBuilder("Operations")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("LIBS", "libs"))
            .addProperty(buildConstProperty("IMPLEMENTATION", "implementation"))
            .addProperty(buildConstProperty("KSP", "ksp"))
            .addProperty(buildConstProperty("TEST_IMPLEMENTATION", "testImplementation"))
            .addProperty(buildConstProperty("ANDROID_TEST_IMPLEMENTATION", "androidTestImplementation"))
            .build()
    }

    private fun buildPluginAliasesObject(diMode: DiMode): TypeSpec {
        val builder = TypeSpec.objectBuilder("PluginAliases")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("ANDROID_LIBRARY", "android-library"))
            .addProperty(buildConstProperty("KOTLIN_ANDROID", "kotlin-android"))
            .addProperty(buildConstProperty("PARCELIZE", "parcelize"))
            .addProperty(buildConstProperty("KOTLIN_SERIALIZATION", "kotlin-serialization"))
            .addProperty(buildConstProperty("COMPOSE_COMPILER", "compose-compiler"))

        if (diMode == DiMode.HILT || diMode == DiMode.KOIN_ANNOTATIONS) {
            builder.addProperty(buildConstProperty("KSP", "ksp"))
        }

        if (diMode == DiMode.HILT) {
            builder.addProperty(buildConstProperty("HILT", "hilt"))
        }
        
        return builder.build()
    }

    private fun buildBuildPropAliasesObject(): TypeSpec {
        return TypeSpec.objectBuilder("BuildPropAliases")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("COMPILE_SDK", "compileSdk"))
            .addProperty(buildConstProperty("MIN_SDK", "minSdk"))
            .addProperty(buildConstProperty("TARGET_SDK", "targetSdk"))
            .addProperty(buildConstProperty("SEMANTIC_VERSION", "semanticVersion"))
            .addProperty(
                PropertySpec.builder("DEFAULT_COMPILE_SDK", Int::class)
                    .addModifiers(KModifier.CONST)
                    .initializer("35")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("DEFAULT_MIN_SDK", Int::class)
                    .addModifiers(KModifier.CONST)
                    .initializer("28")
                    .build()
            )
            .build()
    }

    private fun buildDependenciesObject(diMode: DiMode): TypeSpec {
        return TypeSpec.objectBuilder("Dependencies")
            .addModifiers(KModifier.INTERNAL)
            .addType(buildLibsCommonObject(diMode))
            .addType(buildLibsComposeObject(diMode))
            .addType(buildLibsCoroutinesObject())
            .addType(buildLibsUnitTestObject())
            .addType(buildLibsAndroidTestObject())
            .build()
    }

    private fun buildLibsCommonObject(diMode: DiMode): TypeSpec {
        val builder = TypeSpec.objectBuilder("LibsCommon")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("KOTLINX_SERIALIZATION", "kotlinx-serialization"))
            .addProperty(buildConstProperty("JSON", "json"))
            .addProperty(buildConstProperty("CORE_KTX", "androidx-core-ktx"))

        when (diMode) {
            DiMode.HILT -> {
                builder.addProperty(buildConstProperty("HILT", "hilt"))
                builder.addProperty(buildConstProperty("HILT_COMPILER", "hilt-compiler"))
            }
            DiMode.KOIN -> {
                builder.addProperty(buildConstProperty("KOIN", "koin"))
            }
            DiMode.KOIN_ANNOTATIONS -> {
                builder.addProperty(buildConstProperty("KOIN_CORE", "koin-core"))
                builder.addProperty(buildConstProperty("KOIN_ANNOTATIONS", "koin-annotations"))
                builder.addProperty(buildConstProperty("KOIN_KSP_COMPILER", "koin-ksp-compiler"))
            }
        }
        return builder.build()
    }

    private fun buildLibsComposeObject(diMode: DiMode): TypeSpec {
        val builder = TypeSpec.objectBuilder("LibsCompose")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("UI", "compose-ui"))
            .addProperty(buildConstProperty("MATERIAL3_ANDROID", "compose-material3-android"))
            .addProperty(buildConstProperty("NAVIGATION", "compose-navigation"))
            .addProperty(buildConstProperty("TOOLING", "compose-tooling"))
            .addProperty(buildConstProperty("PREVIEW", "compose-preview"))

        if (diMode == DiMode.HILT) {
            builder.addProperty(buildConstProperty("HILT_NAVIGATION", "compose-hilt-navigation"))
        } else {
            builder.addProperty(buildConstProperty("KOIN_NAVIGATION", "compose-koin-navigation"))
        }
        return builder.build()
    }

    private fun buildLibsCoroutinesObject(): TypeSpec {
        return TypeSpec.objectBuilder("LibsCoroutines")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("CORE", "coroutines-core"))
            .addProperty(buildConstProperty("ANDROID", "coroutines-android"))
            .build()
    }

    private fun buildLibsUnitTestObject(): TypeSpec {
        return TypeSpec.objectBuilder("LibsUnitTest")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("KOTEST_RUNNER", "kotest-runner"))
            .addProperty(buildConstProperty("KOTEST_ASSERTION", "kotest-assertion"))
            .addProperty(buildConstProperty("KOTEST_PROPERTY", "kotest-property"))
            .addProperty(buildConstProperty("MOCKK", "mockk"))
            .addProperty(buildConstProperty("COROUTINES_TEST", "coroutines-test"))
            .addProperty(buildConstProperty("TURBINE", "turbine"))
            .addProperty(buildConstProperty("JUNIT_VINTAGE_ENGINE", "junit-vintage-engine"))
            .build()
    }

    private fun buildLibsAndroidTestObject(): TypeSpec {
        return TypeSpec.objectBuilder("LibsAndroidTest")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("ANDROIDX_TEST_RUNNER", "androidx-test-runner"))
            .addProperty(buildConstProperty("COMPOSE_UI_TEST", "compose-ui-test"))
            .addProperty(buildConstProperty("MOCKK_ANDROID", "mockk-android"))
            .addProperty(buildConstProperty("ESPRESSO", "espresso"))
            .addProperty(buildConstProperty("COROUTINES", "coroutines-test"))
            .addProperty(buildConstProperty("NAV_TEST", "androidx-navigation-testing"))
            .build()
    }

    private fun buildConstProperty(name: String, value: String): PropertySpec {
        return PropertySpec.builder(name, String::class)
            .addModifiers(KModifier.CONST)
            .initializer("%S", value)
            .build()
    }
}
