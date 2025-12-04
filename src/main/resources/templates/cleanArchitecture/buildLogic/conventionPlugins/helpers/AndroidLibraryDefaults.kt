package com.${PACKAGE}.convention.helpers

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Shared Android library defaults for Android library convention plugins.
 */
internal fun Project.configureAndroidLibraryDefaults() {
    extensions.configure<LibraryExtension> {
        // Read from root gradle.properties if present; fallback values keep behavior stable
        val compileSdkProp = findProperty("compileSdk")?.toString()?.toIntOrNull() ?: 35
        val minSdkProp = findProperty("minSdk")?.toString()?.toIntOrNull() ?: 28
        val targetSdkProp = findProperty("targetSdk")?.toString()?.toIntOrNull() ?: compileSdkProp

        compileSdk = compileSdkProp

        defaultConfig {
            minSdk = minSdkProp
            targetSdk = targetSdkProp
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        // Enable generation of BuildConfig for library modules (disabled by default in AGP for libraries)
        buildFeatures {
            buildConfig = true
        }
    }

    // Tests
    configureUnitTesting()

    // Standardized unit test library dependencies
    addStandardTestDependencies()

    // Align Kotlin JVM target with Java to avoid mismatch
    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(17)
    }
}
