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
