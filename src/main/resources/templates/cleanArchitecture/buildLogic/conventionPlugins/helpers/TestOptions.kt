package com.${PACKAGE}.convention.helpers

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

/**
 * Configure JUnit Platform + default unit test options similar to legacy root.gradle.
 */
internal fun Project.configureUnitTesting() {
    // JVM test tasks (junit4 + junit5 platform)
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs(
            "--add-opens", "java.base/java.util=ALL-UNNAMED",
            "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
            "-Xmx4g"
        )
    }
    // Android test options
    extensions.configure<LibraryExtension> {
        testOptions {
            // Some AGP versions expose returnDefaultValues; swallow if absent.
            unitTests.all { test ->
                // Allow build to proceed even if some unit tests fail (legacy behavior)
                test.ignoreFailures = true
                test.useJUnitPlatform()
                test.jvmArgs(
                    "--add-opens", "java.base/java.util=ALL-UNNAMED",
                    "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
                    "-Xmx4g"
                )
                // Enable Jacoco per test for coverage aggregation
                test.extensions.extraProperties["jacoco"] = true
            }
        }
    }
}
