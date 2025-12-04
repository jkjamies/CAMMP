package com.${PACKAGE}.convention

import com.${PACKAGE}.convention.helpers.configureAndroidLibraryDefaults
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class PresentationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        val androidLibraryPluginId = libs.findPlugin("library").get().get().pluginId
        val kotlinAndroidPluginId = libs.findPlugin("kotlin").get().get().pluginId
        val parcelizePluginId = libs.findPlugin("parcelize").get().get().pluginId
        val kotlinSerializationPluginId = libs.findPlugin("kotlin-serialization").get().get().pluginId
        val kspPluginId = libs.findPlugin("ksp").get().get().pluginId

        pluginManager.apply(androidLibraryPluginId)
        pluginManager.apply(kotlinAndroidPluginId)
        pluginManager.apply(parcelizePluginId)
        pluginManager.apply(kotlinSerializationPluginId)
        pluginManager.apply(kspPluginId)

        configureAndroidLibraryDefaults()

        val koinAndroid = libs.findLibrary("koin-android").orNull
        val koinAnnotations = libs.findLibrary("koin-annotations").orNull
        val koinKspCompiler = libs.findLibrary("koin-ksp-compiler").orNull
        val kotlinxSerialization = libs.findLibrary("kotlinx-serialization").orNull

        dependencies {
            koinAndroid?.let { addProvider("implementation", it) }
            koinAnnotations?.let { addProvider("implementation", it) }
            koinKspCompiler?.let { addProvider("ksp", it) }
            kotlinxSerialization?.let { addProvider("implementation", it) }
        }
    }
}
