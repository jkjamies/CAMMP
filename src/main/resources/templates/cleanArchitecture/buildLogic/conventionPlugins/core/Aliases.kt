package com.${PACKAGE}.convention.core

internal object Aliases {
    internal object Operations {
        const val LIBS = "libs"
        const val IMPLEMENTATION = "implementation"
        const val KSP = "ksp"
        const val TEST_IMPLEMENTATION = "testImplementation"
        const val ANDROID_TEST_IMPLEMENTATION = "androidTestImplementation"
    }

    internal object PluginAliases {
        const val ANDROID_LIBRARY = "android-library"
        const val KOTLIN_ANDROID = "kotlin-android"
        const val KSP = "ksp"
        const val HILT = "hilt"
        const val PARCELIZE = "parcelize"
        const val KOTLIN_SERIALIZATION = "kotlin-serialization"
        const val COMPOSE_COMPILER = "compose-compiler"
    }

    internal object BuildPropAliases {
        // Keys (gradle.properties)
        const val COMPILE_SDK = "compileSdk"
        const val MIN_SDK = "minSdk"
        const val TARGET_SDK = "targetSdk"
        const val SEMANTIC_VERSION = "semanticVersion"

        // Defaults
        const val DEFAULT_COMPILE_SDK = 35
        const val DEFAULT_MIN_SDK = 28
    }

    internal object Dependencies {
        internal object LibsCommon {
            const val HILT = "hilt"
            const val HILT_COMPILER = "hilt-compiler"
            const val KOTLINX_SERIALIZATION = "kotlinx-serialization"
            const val JSON = "json"
            const val CORE_KTX = "androidx-core-ktx"
        }

        internal object LibsCompose {
            const val UI = "compose-ui"
            const val MATERIAL3_ANDROID = "compose-material3-android"
            const val NAVIGATION = "compose-navigation"
            const val HILT_NAVIGATION = "compose-hilt-navigation"
            const val TOOLING = "compose-tooling"
            const val PREVIEW = "compose-preview"
        }

        internal object LibsCoroutines {
            const val CORE = "coroutines-core"
            const val ANDROID = "coroutines-android"
        }

        internal object LibsUnitTest {
            const val KOTEST_RUNNER = "kotest-runner"
            const val KOTEST_ASSERTION = "kotest-assertion"
            const val KOTEST_PROPERTY = "kotest-property"
            const val MOCKK = "mockk"
            const val COROUTINES_TEST = "coroutines-test"
            const val TURBINE = "turbine"
        }

        internal object LibsAndroidTest {
            const val ANDROIDX_TEST_RUNNER = "androidx-test-runner"
            const val COMPOSE_UI_TEST = "compose-ui-test"
            const val MOCKK_ANDROID = "mockk-android"
            const val ESPRESSO = "espresso"
            const val COROUTINES = "coroutines-test"
            const val NAV_TEST = "androidx-navigation-testing"
        }
    }
}
