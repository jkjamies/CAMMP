package com.${PACKAGE}.convention.helpers

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies

/**
 * Separated from AndroidLibraryDefaults for reuse and clarity.
 */
internal fun Project.addStandardTestDependencies() {
    val catalogsExt = rootProject.extensions.findByType(VersionCatalogsExtension::class.java)
    val catalogs = catalogsExt?.named("libs") ?: return

    dependencies {
        catalogs.findLibrary("kotest-runner").ifPresent {
            add("testImplementation", it)
        }
        catalogs.findLibrary("kotest-assertion").ifPresent {
            add("testImplementation", it)
        }
        catalogs.findLibrary("kotest-property").ifPresent {
            add("testImplementation", it)
        }
        catalogs.findLibrary("junit-vintage-engine").ifPresent {
            add("testImplementation", it)
        }
        catalogs.findLibrary("mockk").ifPresent {
            add("testImplementation", it)
        }
        catalogs.findLibrary("coroutines-test").ifPresent {
            add("testImplementation", it)
        }
        catalogs.findLibrary("turbine").ifPresent {
            add("testImplementation", it)
        }
    }
}
