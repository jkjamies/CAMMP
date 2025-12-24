package com.${PACKAGE}.convention.helpers

import com.android.build.api.dsl.LibraryExtension
import com.${PACKAGE}.convention.core.Aliases.BuildPropAliases.COMPILE_SDK
import com.${PACKAGE}.convention.core.Aliases.BuildPropAliases.DEFAULT_COMPILE_SDK
import com.${PACKAGE}.convention.core.Aliases.BuildPropAliases.DEFAULT_MIN_SDK
import com.${PACKAGE}.convention.core.Aliases.BuildPropAliases.MIN_SDK
import com.${PACKAGE}.convention.core.Aliases.BuildPropAliases.SEMANTIC_VERSION
import com.${PACKAGE}.convention.core.Aliases.BuildPropAliases.TARGET_SDK
import com.${PACKAGE}.convention.core.propInt
import com.${PACKAGE}.convention.core.propString
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Shared Android library defaults for Android library convention plugins.
 */
internal fun Project.configureAndroidLibraryDefaults() {
    extensions.configure<LibraryExtension> {
        // Read from root gradle.properties if present; fallback values keep behavior stable
        val compileSdkProp = propInt(COMPILE_SDK, DEFAULT_COMPILE_SDK)
        val minSdkProp = propInt(MIN_SDK, DEFAULT_MIN_SDK)
        val targetSdkProp = propInt(TARGET_SDK, compileSdkProp)

        compileSdk = compileSdkProp

        defaultConfig {
            minSdk = minSdkProp
            // TODO: remove targetSdk if using AGP v9.0
            targetSdk = targetSdkProp
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            // Optional build config field if semanticVersion provided
            propString(SEMANTIC_VERSION)?.let { sv ->
                buildConfigField("String", "SEMANTIC_VERSION_MARKET", "\"$sv\"")
            }
            useLibrary("org.apache.http.legacy")
        }

        // Enable generation of BuildConfig for library modules (disabled by default in AGP for libraries)
        buildFeatures {
            buildConfig = true
        }

        // Set unit tests to return default values for unmocked methods
        testOptions {
            unitTests.isReturnDefaultValues = true
        }
    }

    // Lint options and wiring custom lint checks
    // TODO: create your own
//    configureLintOptions()

    // Flavors & build types
    // TODO: create your own market flavors if you need them
//    configureMarketFlavors()
    // TODO: create your own configuration for debug/release/etc if you need them
//    configureStandardBuildTypes()

    // Tests
    configureUnitTesting()

    // Standardized unit test library dependencies
    addStandardTestDependencies()

    // Align Kotlin JVM target with Java to avoid mismatch
    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(17)
    }
}
