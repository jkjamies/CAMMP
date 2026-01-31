/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.ConventionPluginRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.PluginType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

@ContributesBinding(AppScope::class)
class ConventionPluginRepositoryImpl(
    private val fs: FileSystemRepository
) : ConventionPluginRepository {

    // region Public API
    override fun generate(
        outputDirectory: Path,
        packageName: String,
        diMode: DiMode,
        type: PluginType
    ) {
        val fileSpec = when (type) {
            PluginType.DATA -> generateDataPlugin(packageName, diMode)
            PluginType.DI -> generateDiPlugin(packageName, diMode)
            PluginType.DOMAIN -> generateDomainPlugin(packageName, diMode)
            PluginType.PRESENTATION -> generatePresentationPlugin(packageName, diMode)
            PluginType.DATA_SOURCE -> generateDataSourcePlugin(packageName, diMode, "DataSource")
            PluginType.REMOTE_DATA_SOURCE -> generateDataSourcePlugin(packageName, diMode, "RemoteDataSource")
            PluginType.LOCAL_DATA_SOURCE -> generateDataSourcePlugin(packageName, diMode, "LocalDataSource")
        }
        val outputFile = outputDirectory.resolve(fileSpec.name + ".kt")
        fs.writeText(outputFile, fileSpec.toString())
    }
    // endregion

    // region Plugin-Specific Generators
    private fun generateDataPlugin(packageName: String, diMode: DiMode): FileSpec {
        val applyBlock = CodeBlock.builder()
            .addCommonPreamble(packageName)
            .addPluginBlock(packageName, diMode, includeCompose = false)
            .addCommonAndroidBlock(packageName)
            .addDependenciesBlock(packageName) {
                addDiDependencies(it, packageName, diMode)
                addCommonDependencies(it, packageName)
                addTestDependencies(it, packageName)
            }
            .addEndControlFlow() // end with(target) {...} block
            .build()
        return createPluginFile(packageName, "DataConventionPlugin", applyBlock)
    }

    private fun generateDiPlugin(packageName: String, diMode: DiMode): FileSpec {
        val applyBlock = CodeBlock.builder()
            .addCommonPreamble(packageName)
            .addPluginBlock(packageName, diMode, includeCompose = false)
            .addCommonAndroidBlock(packageName)
            .addDependenciesBlock(packageName) {
                addDiDependencies(it, packageName, diMode)
                addCommonDependencies(it, packageName)
            }
            .addEndControlFlow()
            .build()
        return createPluginFile(packageName, "DIConventionPlugin", applyBlock)
    }

    private fun generateDomainPlugin(packageName: String, diMode: DiMode): FileSpec {
        val applyBlock = CodeBlock.builder()
            .addCommonPreamble(packageName)
            .addPluginBlock(packageName, diMode, includeCompose = false)
            .addCommonAndroidBlock(packageName)
            .addDependenciesBlock(packageName) {
                addDiDependencies(it, packageName, diMode)
                addCommonDependencies(it, packageName)
            }
            .addEndControlFlow()
            .build()
        return createPluginFile(packageName, "DomainConventionPlugin", applyBlock)
    }

    private fun generateDataSourcePlugin(packageName: String, diMode: DiMode, name: String): FileSpec {
        val applyBlock = CodeBlock.builder()
            .addCommonPreamble(packageName)
            .addPluginBlock(packageName, diMode, includeCompose = false)
            .addCommonAndroidBlock(packageName)
            .addDependenciesBlock(packageName) {
                addDiDependencies(it, packageName, diMode)
                addCommonDependencies(it, packageName)
            }
            .addEndControlFlow()
            .build()
        return createPluginFile(packageName, "${name}ConventionPlugin", applyBlock)
    }

    private fun generatePresentationPlugin(packageName: String, diMode: DiMode): FileSpec {
        val applyBlock = CodeBlock.builder()
            .addCommonPreamble(packageName)
            .addPluginBlock(packageName, diMode, includeCompose = true)
            .addPresentationAndroidBlock(packageName)
            .addDependenciesBlock(packageName) {
                addDiDependencies(it, packageName, diMode)
                addCommonDependencies(it, packageName)
                addPresentationDependencies(it, packageName, diMode)
                addAndroidTestDependencies(it, packageName)
            }
            .addEndControlFlow()
            .build()
        return createPluginFile(packageName, "PresentationConventionPlugin", applyBlock)
    }
    // endregion

    // region CodeBlock Builder Extensions (Helpers)
    private fun CodeBlock.Builder.addCommonPreamble(packageName: String): CodeBlock.Builder {
        val corePackage = "$packageName.core"
        val libsCatalogExt = ClassName(corePackage, "libsCatalog")
        return beginControlFlow("with(target)")
            .addStatement("val libs = %T()", libsCatalogExt)
            .add("\n")
    }

    private fun CodeBlock.Builder.addPluginBlock(packageName: String, diMode: DiMode, includeCompose: Boolean): CodeBlock.Builder {
        val corePackage = "$packageName.core"
        val pluginsExt = ClassName(corePackage, "plugins")
        val pluginAliases = ClassName(corePackage, "Aliases", "PluginAliases")

        add("// Apply plugins via Plugins DSL\n")
        add("libs.%T(this).applyAll(\n", pluginsExt).indent()
        addStatement("%T.ANDROID_LIBRARY,", pluginAliases)
        addStatement("%T.KOTLIN_ANDROID,", pluginAliases)
        if (diMode == DiMode.HILT || diMode == DiMode.KOIN_ANNOTATIONS) {
            addStatement("%T.KSP,", pluginAliases)
        }
        if (diMode == DiMode.HILT) {
            addStatement("%T.HILT,", pluginAliases)
        }
        if (diMode == DiMode.METRO) {
            addStatement("%T.METRO,", pluginAliases)
        }
        addStatement("%T.PARCELIZE,", pluginAliases)
        addStatement("%T.KOTLIN_SERIALIZATION,", pluginAliases)
        if (includeCompose) {
            addStatement("%T.COMPOSE_COMPILER", pluginAliases)
        }
        unindent().addStatement(")")
        return this
    }

    private fun CodeBlock.Builder.addCommonAndroidBlock(packageName: String): CodeBlock.Builder {
        val helpersPackage = "$packageName.helpers"
        val configureDefaults = ClassName(helpersPackage, "configureAndroidLibraryDefaults")
        add("\n")
        add("// Configure Android library defaults\n")
        addStatement("%T()", configureDefaults)
        return this
    }

    private fun CodeBlock.Builder.addPresentationAndroidBlock(packageName: String): CodeBlock.Builder {
        val libraryExtension = ClassName("com.android.build.api.dsl", "LibraryExtension")
        val configureExt = ClassName("org.gradle.kotlin.dsl", "configure")
        addCommonAndroidBlock(packageName)
        add("\n")
        add("// Additional Android library configuration for presentation layer\n")
        beginControlFlow("extensions.%T<%T>", configureExt, libraryExtension)
        addStatement("buildFeatures { compose = true }")
        beginControlFlow("packaging")
        beginControlFlow("resources")
        addStatement("excludes += \"META-INF/*\"")
        addStatement("excludes += \"draftv4/schema\"")
        addStatement("excludes += \"draftv3/schema\"")
        addStatement("excludes += \"google/protobuf/*\"")
        endControlFlow()
        endControlFlow()
        endControlFlow()
        return this
    }

    private fun CodeBlock.Builder.addDependenciesBlock(packageName: String, builder: (CodeBlock.Builder) -> Unit): CodeBlock.Builder {
        val corePackage = "$packageName.core"
        val dependenciesExt = ClassName(corePackage, "dependencies")
        add("\n")
        add("// Add dependencies via Dependencies DSL with shared libs\n")
        addStatement("val deps = libs.%T(this)", dependenciesExt)
        builder(this)
        return this
    }

    private fun addDiDependencies(builder: CodeBlock.Builder, packageName: String, diMode: DiMode) {
        val corePackage = "$packageName.core"
        val libsCommon = ClassName(corePackage, "Aliases", "Dependencies", "LibsCommon")
        when (diMode) {
            DiMode.HILT -> {
                builder.addStatement("deps.implementation(%T.HILT)", libsCommon)
                builder.addStatement("deps.ksp(%T.HILT_COMPILER)", libsCommon)
            }
            DiMode.KOIN -> {
                builder.addStatement("deps.implementation(%T.KOIN)", libsCommon)
            }
            DiMode.KOIN_ANNOTATIONS -> {
                builder.addStatement("deps.implementation(%T.KOIN_CORE)", libsCommon)
                builder.addStatement("deps.implementation(%T.KOIN_ANNOTATIONS)", libsCommon)
                builder.addStatement("deps.ksp(%T.KOIN_KSP_COMPILER)", libsCommon)
            }
            DiMode.METRO -> Unit // Metro uses only the plugin, no dependencies needed here
        }
    }

    private fun addCommonDependencies(builder: CodeBlock.Builder, packageName: String) {
        val corePackage = "$packageName.core"
        val libsCommon = ClassName(corePackage, "Aliases", "Dependencies", "LibsCommon")
        builder.addStatement("deps.implementation(%T.KOTLINX_SERIALIZATION)", libsCommon)
    }

    private fun addPresentationDependencies(builder: CodeBlock.Builder, packageName: String, diMode: DiMode) {
        val corePackage = "$packageName.core"
        val libsCommon = ClassName(corePackage, "Aliases", "Dependencies", "LibsCommon")
        val libsCompose = ClassName(corePackage, "Aliases", "Dependencies", "LibsCompose")
        val libsCoroutines = ClassName(corePackage, "Aliases", "Dependencies", "LibsCoroutines")
        builder.addStatement("deps.implementationPlatform(%T.COMPOSE_BOM)", libsCompose)
        builder.addStatement("deps.implementation(%T.ANDROID)", libsCoroutines)
        builder.addStatement("deps.implementation(%T.CORE)", libsCoroutines)
        builder.addStatement("deps.implementation(%T.MATERIAL3_ANDROID)", libsCompose)
        builder.addStatement("deps.implementation(%T.CORE_KTX)", libsCommon)
        builder.addStatement("deps.implementation(%T.UI)", libsCompose)
        builder.addStatement("deps.implementation(%T.UI_GRAPHICS)", libsCompose)
        builder.addStatement("deps.implementation(%T.NAVIGATION)", libsCompose)
        if (diMode == DiMode.HILT) {
            builder.addStatement("deps.implementation(%T.HILT_NAVIGATION)", libsCompose)
        } else if (diMode == DiMode.KOIN || diMode == DiMode.KOIN_ANNOTATIONS) {
            builder.addStatement("deps.implementation(%T.KOIN_NAVIGATION)", libsCompose)
        }
        // METRO: No navigation integration yet
        builder.addStatement("deps.debugImplementation(%T.TOOLING)", libsCompose)
        builder.addStatement("deps.debugImplementation(%T.PREVIEW)", libsCompose)
    }

    private fun addTestDependencies(builder: CodeBlock.Builder, packageName: String) {
        val corePackage = "$packageName.core"
        val libsCommon = ClassName(corePackage, "Aliases", "Dependencies", "LibsCommon")
        builder.addStatement("deps.testImplementation(%T.JSON)", libsCommon)
    }

    private fun addAndroidTestDependencies(builder: CodeBlock.Builder, packageName: String) {
        val corePackage = "$packageName.core"
        val libsAndroidTest = ClassName(corePackage, "Aliases", "Dependencies", "LibsAndroidTest")
        val libsCompose = ClassName(corePackage, "Aliases", "Dependencies", "LibsCompose")
        builder.add("\n// Instrumented test dependencies via DSL\n")
        builder.addStatement("deps.androidTestImplementation(%T.ANDROIDX_TEST_RUNNER)", libsAndroidTest)
        builder.addStatement("deps.androidTestImplementation(%T.COMPOSE_UI_TEST)", libsAndroidTest)
        builder.addStatement("deps.androidTestImplementation(%T.MOCKK_ANDROID)", libsAndroidTest)
        builder.addStatement("deps.androidTestImplementation(%T.COROUTINES)", libsAndroidTest)
        builder.addStatement("deps.androidTestImplementation(%T.ESPRESSO)", libsAndroidTest)
        builder.addStatement("deps.androidTestImplementation(%T.NAV_TEST)", libsAndroidTest)
        builder.addStatement("deps.debugImplementation(%T.UI_TEST_MANIFEST)", libsCompose)
    }
    
    private fun CodeBlock.Builder.addEndControlFlow(): CodeBlock.Builder {
        return endControlFlow()
    }
    // endregion

    // region File Creation
    private fun createPluginFile(packageName: String, className: String, applyBlock: CodeBlock): FileSpec {
        val projectClass = ClassName("org.gradle.api", "Project")
        val pluginInterface = ClassName("org.gradle.api", "Plugin").parameterizedBy(projectClass)
        val corePackage = "$packageName.core"

        val fileBuilder = FileSpec.builder(packageName, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .addModifiers(KModifier.INTERNAL)
                    .addSuperinterface(pluginInterface)
                    .addFunction(
                        FunSpec.builder("apply")
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter("target", projectClass)
                            .addCode(applyBlock)
                            .build()
                    )
                    .build()
            )
        
        // Add static imports for Aliases
        fileBuilder.addImport("$corePackage.Aliases", "PluginAliases")
        fileBuilder.addImport("$corePackage.Aliases.Dependencies", "LibsCommon", "LibsAndroidTest", "LibsCompose", "LibsCoroutines")

        return fileBuilder.build()
    }
    // endregion
}