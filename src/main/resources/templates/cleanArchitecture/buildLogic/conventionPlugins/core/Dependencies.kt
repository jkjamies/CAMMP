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
    fun testImplementation(alias: String) = add(Operations.TEST_IMPLEMENTATION, alias)
    fun androidTestImplementation(alias: String) =
        add(Operations.ANDROID_TEST_IMPLEMENTATION, alias)

    private fun add(configuration: String, alias: String) {
        val provider: Provider<MinimalExternalModuleDependency> = libs.findLibrary(alias).get()
        project.dependencies {
            addProvider(configuration, provider)
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
