package com.${PACKAGE}.convention

import com.${PACKAGE}.convention.core.Aliases.PluginAliases
import com.${PACKAGE}.convention.core.Aliases.Dependencies.LibsCommon
import com.android.build.api.dsl.LibraryExtension
import com.${PACKAGE}.convention.core.Aliases.Dependencies.LibsAndroidTest
import com.${PACKAGE}.convention.core.Aliases.Dependencies.LibsCompose
import com.${PACKAGE}.convention.core.Aliases.Dependencies.LibsCoroutines
import com.${PACKAGE}.convention.core.dependencies
import com.${PACKAGE}.convention.core.libsCatalog
import com.${PACKAGE}.convention.core.plugins
import com.${PACKAGE}.convention.helpers.configureAndroidLibraryDefaults
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal class PresentationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = libsCatalog()

            // Apply plugins via Plugins DSL
            libs.plugins(this).applyAll(
                PluginAliases.ANDROID_LIBRARY,
                PluginAliases.KOTLIN_ANDROID,
                PluginAliases.KSP,
                PluginAliases.PARCELIZE,
                PluginAliases.KOTLIN_SERIALIZATION,
                PluginAliases.COMPOSE_COMPILER
            )

            // Apply Jacoco script
            apply { from(rootProject.file("scripts/jacoco.gradle")) }

            // Configure Android library defaults
            configureAndroidLibraryDefaults()

            // Additional Android library configuration for presentation layer
            extensions.configure<LibraryExtension> {
                buildFeatures { compose = true }
                packaging {
                    resources {
                        excludes += "META-INF/*"
                        excludes += "draftv4/schema"
                        excludes += "draftv3/schema"
                        excludes += "google/protobuf/*"
                    }
                }
            }

            // Add dependencies via Dependencies DSL with shared libs
            val deps = libs.dependencies(this)
            deps.implementation(LibsCommon.KOIN_CORE)
            deps.implementation(LibsCommon.KOIN_ANNOTATIONS)
            deps.implementation(LibsCommon.KOTLINX_SERIALIZATION)
            deps.implementation(LibsCoroutines.ANDROID)
            deps.implementation(LibsCoroutines.CORE)
            deps.implementation(LibsCompose.MATERIAL3_ANDROID)
            deps.implementation(LibsCommon.CORE_KTX)
            deps.implementation(LibsCompose.UI)
            deps.implementation(LibsCompose.NAVIGATION)
            deps.implementation(LibsCompose.KOIN_NAVIGATION)
            deps.implementation(LibsCompose.TOOLING)
            deps.implementation(LibsCompose.PREVIEW)
            deps.ksp(LibsCommon.KOIN_KSP_COMPILER)

            // Instrumented test dependencies via DSL
            deps.androidTestImplementation(LibsAndroidTest.ANDROIDX_TEST_RUNNER)
            deps.androidTestImplementation(LibsAndroidTest.COMPOSE_UI_TEST)
            deps.androidTestImplementation(LibsAndroidTest.MOCKK_ANDROID)
            deps.androidTestImplementation(LibsAndroidTest.COROUTINES)
            deps.androidTestImplementation(LibsAndroidTest.ESPRESSO)
            deps.androidTestImplementation(LibsAndroidTest.NAV_TEST)
        }
    }
}
