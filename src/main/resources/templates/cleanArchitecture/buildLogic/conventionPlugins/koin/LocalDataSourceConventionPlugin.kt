package com.${PACKAGE}.convention

import com.${PACKAGE}.convention.helpers.configureAndroidLibraryDefaults
import com.${PACKAGE}.convention.core.Aliases.PluginAliases
import com.${PACKAGE}.convention.core.Aliases.Dependencies.LibsCommon
import com.${PACKAGE}.convention.core.dependencies
import com.${PACKAGE}.convention.core.libsCatalog
import com.${PACKAGE}.convention.core.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class LocalDataSourceConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = libsCatalog()

            // Apply plugins using the Plugins DSL
            libs.plugins(this).applyAll(
                PluginAliases.ANDROID_LIBRARY,
                PluginAliases.KOTLIN_ANDROID,
                PluginAliases.PARCELIZE,
                PluginAliases.KOTLIN_SERIALIZATION
            )

            // Apply Jacoco script
            apply { from(rootProject.file("scripts/jacoco.gradle")) }

            // Configure Android library defaults
            configureAndroidLibraryDefaults()

            // Add dependencies via Dependencies DSL with shared libs
            val deps = libs.dependencies(this)
            deps.implementation(LibsCommon.KOIN)
            deps.implementation(LibsCommon.KOTLINX_SERIALIZATION)
        }
    }
}
