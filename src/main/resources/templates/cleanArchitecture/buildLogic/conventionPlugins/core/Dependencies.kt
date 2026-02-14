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

package com.${PACKAGE}.convention.core

import com.${PACKAGE}.convention.core.Aliases.Operations
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Resolve the "libs" version catalog for the current [Project].
 */
internal fun Project.libsCatalog(): VersionCatalog =
    extensions.getByType<VersionCatalogsExtension>().named(Operations.LIBS)

/**
 * Retrieve an integer property by its [name] from the project, returning [default] if not found or invalid.
 */
internal fun Project.propInt(name: String, default: Int): Int =
    findProperty(name)?.toString()?.toIntOrNull() ?: run {
        logger.warn("[BuildLogic] Project property '$name' is missing or invalid. Using default: $default")
        default
    }

/**
 * Retrieve a string property by its [name] from the project, returning [default] if not found or empty.
 */
internal fun Project.propString(name: String, default: String? = null): String? =
    (findProperty(name) as? String)?.takeIf { it.isNotEmpty() } ?: run {
        logger.warn("[BuildLogic] Project property '$name' is missing or empty. Using default: ${default ?: "<null>"}")
        default
    }

/**
 * Resolve a plugin ID from the version catalog by its [alias] in the given [VersionCatalog].
 */
internal fun VersionCatalog.pluginId(alias: String): String =
    findPlugin(alias).get().get().pluginId

/**
 * Common dependency configuration for the given [project] using the provided [libs] version catalog.
 */
internal class Dependencies(private val project: Project, private val libs: VersionCatalog) {
    fun implementation(alias: String) = add(Operations.IMPLEMENTATION, alias)
    fun ksp(alias: String) = add(Operations.KSP, alias)
    fun implementationPlatform(alias: String) = add(Operations.IMPLEMENTATION, alias, isPlatform = true)
    fun debugImplementation(alias: String) = add(Operations.DEBUG_IMPLEMENTATION, alias)
    fun testImplementation(alias: String) = add(Operations.TEST_IMPLEMENTATION, alias)
    fun androidTestImplementation(alias: String) = add(Operations.ANDROID_TEST_IMPLEMENTATION, alias)
    fun androidTestImplementationPlatform(alias: String) = add(Operations.ANDROID_TEST_IMPLEMENTATION, alias, isPlatform = true)

    private fun add(configuration: String, alias: String, isPlatform: Boolean = false) {
        val provider: Provider<MinimalExternalModuleDependency> = libs.findLibrary(alias).get()
        project.dependencies {
            if (isPlatform) {
                add(configuration, platform(provider))
            } else {
                add(configuration, provider)
            }
        }
    }
}

/** Factory to create a Dependencies adder from the version catalog and current project. */
internal fun VersionCatalog.dependencies(project: Project): Dependencies =
    Dependencies(project, this)

/**
 * DSL to apply Gradle plugins for the given [project] using the provided [libs] version catalog.
 */
internal class Plugins(private val project: Project, private val libs: VersionCatalog) {
    fun apply(alias: String) {
        val id = libs.pluginId(alias)
        project.pluginManager.apply(id)
    }

    fun applyAll(vararg aliases: String) {
        aliases.forEach { apply(it) }
    }
}

/** Factory to create a Plugins applier from the version catalog and current project. */
internal fun VersionCatalog.plugins(project: Project): Plugins = Plugins(project, this)
