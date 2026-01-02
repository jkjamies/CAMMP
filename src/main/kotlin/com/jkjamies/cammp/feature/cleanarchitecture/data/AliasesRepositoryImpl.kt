package com.jkjamies.cammp.feature.cleanarchitecture.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.cleanarchitecture.data.datasource.VersionCatalogDataSource
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AliasesRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

@ContributesBinding(AppScope::class)
@Inject
class AliasesRepositoryImpl(
    private val fs: FileSystemRepository,
    private val versionCatalogDataSource: VersionCatalogDataSource
) : AliasesRepository {

    override fun generateAliases(outputDirectory: Path, packageName: String, diMode: DiMode, tomlPath: Path) {
        val resolvedAliases = resolveAliases(diMode, tomlPath)
        
        val aliasesObject = TypeSpec.objectBuilder("Aliases")
            .addModifiers(KModifier.INTERNAL)
            .addType(buildOperationsObject())
            .addType(buildPluginAliasesObject(diMode, resolvedAliases))
            .addType(buildBuildPropAliasesObject())
            .addType(buildDependenciesObject(diMode, resolvedAliases))
            .build()

        val fileSpec = FileSpec.builder(packageName, "Aliases")
            .addType(aliasesObject)
            .build()

        val outputFile = outputDirectory.resolve("Aliases.kt")
        fs.writeText(outputFile, fileSpec.toString())
    }

    private fun resolveAliases(diMode: DiMode, tomlPath: Path): Map<String, String> {
        val dependencies = getRequiredDependencies(diMode)
        return dependencies.associate { dep ->
            val alias = when (dep) {
                is DependencyDefinition.Library -> versionCatalogDataSource.getLibraryAlias(
                    tomlPath, dep.defaultAlias, dep.group, dep.artifact, dep.version, dep.versionRef
                )
                is DependencyDefinition.Plugin -> versionCatalogDataSource.getPluginAlias(
                    tomlPath, dep.defaultAlias, dep.id, dep.version, dep.versionRef
                )
            }
            dep.aliasKey to alias
        }
    }

    private fun getRequiredDependencies(diMode: DiMode): List<DependencyDefinition> {
        val dependencies = mutableListOf<DependencyDefinition>()

        // Common Libraries
        dependencies.add(DependencyDefinition.Library("KOTLINX_SERIALIZATION", "kotlinx-serialization", "org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.6.0"))
        dependencies.add(DependencyDefinition.Library("JSON", "json", "org.json", "json", "20251224"))
        dependencies.add(DependencyDefinition.Library("CORE_KTX", "androidx-core-ktx", "androidx.core", "core-ktx", "1.17.0"))

        // Compose Libraries (BOM)
        dependencies.add(DependencyDefinition.Library("COMPOSE_BOM", "androidx-compose-bom", "androidx.compose", "compose-bom", "2025.12.01"))
        dependencies.add(DependencyDefinition.Library("UI", "androidx-compose-ui", "androidx.compose.ui", "ui", null))
        dependencies.add(DependencyDefinition.Library("UI_GRAPHICS", "androidx-compose-ui-graphics", "androidx.compose.ui", "ui-graphics", null))
        dependencies.add(DependencyDefinition.Library("TOOLING", "androidx-compose-ui-tooling", "androidx.compose.ui", "ui-tooling", null))
        dependencies.add(DependencyDefinition.Library("PREVIEW", "androidx-compose-ui-tooling-preview", "androidx.compose.ui", "ui-tooling-preview", null))
        dependencies.add(DependencyDefinition.Library("UI_TEST_MANIFEST", "androidx-compose-ui-test-manifest", "androidx.compose.ui", "ui-test-manifest", null))
        dependencies.add(DependencyDefinition.Library("COMPOSE_UI_TEST", "androidx-compose-ui-test-junit4", "androidx.compose.ui", "ui-test-junit4", null))
        dependencies.add(DependencyDefinition.Library("MATERIAL3_ANDROID", "androidx-compose-material3", "androidx.compose.material3", "material3", null))
        
        // Compose Navigation (Not in BOM usually, but user didn't specify versionless for this, keeping versioned for now unless specified otherwise, but using new alias style)
        dependencies.add(DependencyDefinition.Library("NAVIGATION", "compose-navigation", "androidx.navigation", "navigation-compose", "2.9.6"))

        // Coroutines Libraries
        dependencies.add(DependencyDefinition.Library("CORE", "coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.10.2"))
        dependencies.add(DependencyDefinition.Library("ANDROID", "coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android", "1.10.2"))

        // Unit Test Libraries
        dependencies.add(DependencyDefinition.Library("KOTEST_RUNNER", "kotest-runner", "io.kotest", "kotest-runner-junit5", "6.0.7"))
        dependencies.add(DependencyDefinition.Library("KOTEST_ASSERTION", "kotest-assertion", "io.kotest", "kotest-assertions-core", "6.0.7"))
        dependencies.add(DependencyDefinition.Library("KOTEST_PROPERTY", "kotest-property", "io.kotest", "kotest-property", "6.0.7"))
        dependencies.add(DependencyDefinition.Library("MOCKK", "mockk", "io.mockk", "mockk", "1.14.7"))
        dependencies.add(DependencyDefinition.Library("COROUTINES_TEST", "coroutines-test", "org.jetbrains.kotlinx", "kotlinx-coroutines-test", "1.10.2"))
        dependencies.add(DependencyDefinition.Library("TURBINE", "turbine", "app.cash.turbine", "turbine", "1.2.1"))
        dependencies.add(DependencyDefinition.Library("JUNIT_VINTAGE_ENGINE", "junit-vintage-engine", "org.junit.vintage", "junit-vintage-engine", "6.0.1"))

        // Android Test Libraries
        dependencies.add(DependencyDefinition.Library("ANDROIDX_TEST_RUNNER", "androidx-test-runner", "androidx.test", "runner", "1.7.0"))
        dependencies.add(DependencyDefinition.Library("MOCKK_ANDROID", "mockk-android", "io.mockk", "mockk-android", "1.14.7"))
        dependencies.add(DependencyDefinition.Library("ESPRESSO", "espresso", "androidx.test.espresso", "espresso-core", "3.5.1"))
        dependencies.add(DependencyDefinition.Library("COROUTINES", "coroutines-test", "org.jetbrains.kotlinx", "kotlinx-coroutines-test", "1.10.2"))
        dependencies.add(DependencyDefinition.Library("NAV_TEST", "androidx-navigation-testing", "androidx.navigation", "navigation-testing", "2.9.6"))

        // Plugins
        dependencies.add(DependencyDefinition.Plugin("ANDROID_LIBRARY", "android-library", "com.android.library", "8.13.2", "agp"))
        dependencies.add(DependencyDefinition.Plugin("KOTLIN_ANDROID", "kotlin-android", "org.jetbrains.kotlin.android", "2.3.0", "kotlin"))
        dependencies.add(DependencyDefinition.Plugin("PARCELIZE", "parcelize", "org.jetbrains.kotlin.plugin.parcelize", "2.3.0", "kotlin"))
        dependencies.add(DependencyDefinition.Plugin("KOTLIN_SERIALIZATION", "kotlin-serialization", "org.jetbrains.kotlin.plugin.serialization", "2.3.0", "kotlin"))
        dependencies.add(DependencyDefinition.Plugin("COMPOSE_COMPILER", "compose-compiler", "org.jetbrains.kotlin.plugin.compose", "2.3.0", "kotlin"))

        // DI Specific
        when (diMode) {
            DiMode.HILT -> {
                dependencies.add(DependencyDefinition.Library("HILT", "hilt", "com.google.dagger", "hilt-android", "2.57.2"))
                dependencies.add(DependencyDefinition.Library("HILT_COMPILER", "hilt-compiler", "com.google.dagger", "hilt-android-compiler", "2.57.2"))
                dependencies.add(DependencyDefinition.Library("HILT_NAVIGATION", "compose-hilt-navigation", "androidx.hilt", "hilt-navigation-compose", "1.3.0"))
                dependencies.add(DependencyDefinition.Plugin("HILT", "hilt", "com.google.dagger.hilt.android", "2.57.2"))
                dependencies.add(DependencyDefinition.Plugin("KSP", "ksp", "com.google.devtools.ksp", "2.3.1"))
            }
            DiMode.KOIN -> {
                dependencies.add(DependencyDefinition.Library("KOIN", "koin", "io.insert-koin", "koin-android", "3.5.0"))
                dependencies.add(DependencyDefinition.Library("KOIN_NAVIGATION", "compose-koin-navigation", "io.insert-koin", "koin-androidx-compose", "3.5.0"))
            }
            DiMode.KOIN_ANNOTATIONS -> {
                dependencies.add(DependencyDefinition.Library("KOIN_CORE", "koin-core", "io.insert-koin", "koin-core", "3.5.0"))
                dependencies.add(DependencyDefinition.Library("KOIN_ANNOTATIONS", "koin-annotations", "io.insert-koin", "koin-annotations", "1.3.0"))
                dependencies.add(DependencyDefinition.Library("KOIN_KSP_COMPILER", "koin-ksp-compiler", "io.insert-koin", "koin-ksp-compiler", "1.3.0"))
                dependencies.add(DependencyDefinition.Library("KOIN_NAVIGATION", "compose-koin-navigation", "io.insert-koin", "koin-androidx-compose", "3.5.0"))
                dependencies.add(DependencyDefinition.Plugin("KSP", "ksp", "com.google.devtools.ksp", "2.3.1"))
            }
        }

        return dependencies
    }

    private fun buildOperationsObject(): TypeSpec {
        return TypeSpec.objectBuilder("Operations")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("LIBS", "libs"))
            .addProperty(buildConstProperty("IMPLEMENTATION", "implementation"))
            .addProperty(buildConstProperty("KSP", "ksp"))
            .addProperty(buildConstProperty("TEST_IMPLEMENTATION", "testImplementation"))
            .addProperty(buildConstProperty("ANDROID_TEST_IMPLEMENTATION", "androidTestImplementation"))
            .addProperty(buildConstProperty("DEBUG_IMPLEMENTATION", "debugImplementation"))
            .build()
    }

    private fun buildPluginAliasesObject(diMode: DiMode, resolvedAliases: Map<String, String>): TypeSpec {
        val builder = TypeSpec.objectBuilder("PluginAliases")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("ANDROID_LIBRARY", resolvedAliases["ANDROID_LIBRARY"] ?: "android-library"))
            .addProperty(buildConstProperty("KOTLIN_ANDROID", resolvedAliases["KOTLIN_ANDROID"] ?: "kotlin-android"))
            .addProperty(buildConstProperty("PARCELIZE", resolvedAliases["PARCELIZE"] ?: "parcelize"))
            .addProperty(buildConstProperty("KOTLIN_SERIALIZATION", resolvedAliases["KOTLIN_SERIALIZATION"] ?: "kotlin-serialization"))
            .addProperty(buildConstProperty("COMPOSE_COMPILER", resolvedAliases["COMPOSE_COMPILER"] ?: "compose-compiler"))

        if (diMode == DiMode.HILT || diMode == DiMode.KOIN_ANNOTATIONS) {
            builder.addProperty(buildConstProperty("KSP", resolvedAliases["KSP"] ?: "ksp"))
        }

        if (diMode == DiMode.HILT) {
            builder.addProperty(buildConstProperty("HILT", resolvedAliases["HILT"] ?: "hilt"))
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

    private fun buildDependenciesObject(diMode: DiMode, resolvedAliases: Map<String, String>): TypeSpec {
        return TypeSpec.objectBuilder("Dependencies")
            .addModifiers(KModifier.INTERNAL)
            .addType(buildLibsCommonObject(diMode, resolvedAliases))
            .addType(buildLibsComposeObject(diMode, resolvedAliases))
            .addType(buildLibsCoroutinesObject(resolvedAliases))
            .addType(buildLibsUnitTestObject(resolvedAliases))
            .addType(buildLibsAndroidTestObject(resolvedAliases))
            .build()
    }

    private fun buildLibsCommonObject(diMode: DiMode, resolvedAliases: Map<String, String>): TypeSpec {
        val builder = TypeSpec.objectBuilder("LibsCommon")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("KOTLINX_SERIALIZATION", resolvedAliases["KOTLINX_SERIALIZATION"] ?: "kotlinx-serialization"))
            .addProperty(buildConstProperty("JSON", resolvedAliases["JSON"] ?: "json"))
            .addProperty(buildConstProperty("CORE_KTX", resolvedAliases["CORE_KTX"] ?: "androidx-core-ktx"))

        when (diMode) {
            DiMode.HILT -> {
                builder.addProperty(buildConstProperty("HILT", resolvedAliases["HILT"] ?: "hilt"))
                builder.addProperty(buildConstProperty("HILT_COMPILER", resolvedAliases["HILT_COMPILER"] ?: "hilt-compiler"))
            }
            DiMode.KOIN -> {
                builder.addProperty(buildConstProperty("KOIN", resolvedAliases["KOIN"] ?: "koin"))
            }
            DiMode.KOIN_ANNOTATIONS -> {
                builder.addProperty(buildConstProperty("KOIN_CORE", resolvedAliases["KOIN_CORE"] ?: "koin-core"))
                builder.addProperty(buildConstProperty("KOIN_ANNOTATIONS", resolvedAliases["KOIN_ANNOTATIONS"] ?: "koin-annotations"))
                builder.addProperty(buildConstProperty("KOIN_KSP_COMPILER", resolvedAliases["KOIN_KSP_COMPILER"] ?: "koin-ksp-compiler"))
            }
        }
        return builder.build()
    }

    private fun buildLibsComposeObject(diMode: DiMode, resolvedAliases: Map<String, String>): TypeSpec {
        val builder = TypeSpec.objectBuilder("LibsCompose")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("COMPOSE_BOM", resolvedAliases["COMPOSE_BOM"] ?: "androidx-compose-bom"))
            .addProperty(buildConstProperty("UI", resolvedAliases["UI"] ?: "androidx-compose-ui"))
            .addProperty(buildConstProperty("UI_GRAPHICS", resolvedAliases["UI_GRAPHICS"] ?: "androidx-compose-ui-graphics"))
            .addProperty(buildConstProperty("MATERIAL3_ANDROID", resolvedAliases["MATERIAL3_ANDROID"] ?: "androidx-compose-material3"))
            .addProperty(buildConstProperty("NAVIGATION", resolvedAliases["NAVIGATION"] ?: "compose-navigation"))
            .addProperty(buildConstProperty("TOOLING", resolvedAliases["TOOLING"] ?: "androidx-compose-ui-tooling"))
            .addProperty(buildConstProperty("PREVIEW", resolvedAliases["PREVIEW"] ?: "androidx-compose-ui-tooling-preview"))
            .addProperty(buildConstProperty("UI_TEST_MANIFEST", resolvedAliases["UI_TEST_MANIFEST"] ?: "androidx-compose-ui-test-manifest"))

        if (diMode == DiMode.HILT) {
            builder.addProperty(buildConstProperty("HILT_NAVIGATION", resolvedAliases["HILT_NAVIGATION"] ?: "compose-hilt-navigation"))
        } else {
            builder.addProperty(buildConstProperty("KOIN_NAVIGATION", resolvedAliases["KOIN_NAVIGATION"] ?: "compose-koin-navigation"))
        }
        return builder.build()
    }

    private fun buildLibsCoroutinesObject(resolvedAliases: Map<String, String>): TypeSpec {
        return TypeSpec.objectBuilder("LibsCoroutines")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("CORE", resolvedAliases["CORE"] ?: "coroutines-core"))
            .addProperty(buildConstProperty("ANDROID", resolvedAliases["ANDROID"] ?: "coroutines-android"))
            .build()
    }

    private fun buildLibsUnitTestObject(resolvedAliases: Map<String, String>): TypeSpec {
        return TypeSpec.objectBuilder("LibsUnitTest")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("KOTEST_RUNNER", resolvedAliases["KOTEST_RUNNER"] ?: "kotest-runner"))
            .addProperty(buildConstProperty("KOTEST_ASSERTION", resolvedAliases["KOTEST_ASSERTION"] ?: "kotest-assertion"))
            .addProperty(buildConstProperty("KOTEST_PROPERTY", resolvedAliases["KOTEST_PROPERTY"] ?: "kotest-property"))
            .addProperty(buildConstProperty("MOCKK", resolvedAliases["MOCKK"] ?: "mockk"))
            .addProperty(buildConstProperty("COROUTINES_TEST", resolvedAliases["COROUTINES_TEST"] ?: "coroutines-test"))
            .addProperty(buildConstProperty("TURBINE", resolvedAliases["TURBINE"] ?: "turbine"))
            .addProperty(buildConstProperty("JUNIT_VINTAGE_ENGINE", resolvedAliases["JUNIT_VINTAGE_ENGINE"] ?: "junit-vintage-engine"))
            .build()
    }

    private fun buildLibsAndroidTestObject(resolvedAliases: Map<String, String>): TypeSpec {
        return TypeSpec.objectBuilder("LibsAndroidTest")
            .addModifiers(KModifier.INTERNAL)
            .addProperty(buildConstProperty("ANDROIDX_TEST_RUNNER", resolvedAliases["ANDROIDX_TEST_RUNNER"] ?: "androidx-test-runner"))
            .addProperty(buildConstProperty("COMPOSE_UI_TEST", resolvedAliases["COMPOSE_UI_TEST"] ?: "androidx-compose-ui-test-junit4"))
            .addProperty(buildConstProperty("MOCKK_ANDROID", resolvedAliases["MOCKK_ANDROID"] ?: "mockk-android"))
            .addProperty(buildConstProperty("ESPRESSO", resolvedAliases["ESPRESSO"] ?: "espresso"))
            .addProperty(buildConstProperty("COROUTINES", resolvedAliases["COROUTINES"] ?: "coroutines-test"))
            .addProperty(buildConstProperty("NAV_TEST", resolvedAliases["NAV_TEST"] ?: "androidx-navigation-testing"))
            .build()
    }

    private fun buildConstProperty(name: String, value: String): PropertySpec {
        return PropertySpec.builder(name, String::class)
            .addModifiers(KModifier.CONST)
            .initializer("%S", value)
            .build()
    }

    // Private data structure for internal use
    private sealed class DependencyDefinition {
        abstract val aliasKey: String
        abstract val defaultAlias: String

        data class Library(
            override val aliasKey: String,
            override val defaultAlias: String,
            val group: String,
            val artifact: String,
            val version: String?,
            val versionRef: String? = null
        ) : DependencyDefinition()

        data class Plugin(
            override val aliasKey: String,
            override val defaultAlias: String,
            val id: String,
            val version: String,
            val versionRef: String? = null
        ) : DependencyDefinition()
    }
}
