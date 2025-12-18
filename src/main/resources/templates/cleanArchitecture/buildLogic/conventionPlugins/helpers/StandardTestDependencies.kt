package com.${PACKAGE}.convention.helpers

import com.${PACKAGE}.convention.core.Aliases.Dependencies.LibsUnitTest
import com.${PACKAGE}.convention.core.dependencies
import com.${PACKAGE}.convention.core.libsCatalog
import org.gradle.api.Project

/**
 * Separated from AndroidLibraryDefaults for reuse and clarity.
 */
internal fun Project.addStandardTestDependencies() {
    val libs = libsCatalog()
    val deps = libs.dependencies(this)

    deps.testImplementation(LibsUnitTest.KOTEST_RUNNER)
    deps.testImplementation(LibsUnitTest.KOTEST_ASSERTION)
    deps.testImplementation(LibsUnitTest.KOTEST_PROPERTY)
    deps.testImplementation(LibsUnitTest.JUNIT_VINTAGE_ENGINE)
    deps.testImplementation(LibsUnitTest.MOCKK)
    deps.testImplementation(LibsUnitTest.COROUTINES_TEST)
    deps.testImplementation(LibsUnitTest.TURBINE)
}
