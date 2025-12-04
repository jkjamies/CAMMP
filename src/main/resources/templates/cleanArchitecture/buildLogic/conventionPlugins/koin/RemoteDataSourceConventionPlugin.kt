package com.${PACKAGE}.convention

import com.${PACKAGE}.convention.helpers.configureAndroidLibraryDefaults
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class RemoteDataSourceConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        val androidLibraryPluginId = libs.findPlugin("library").get().get().pluginId
        val kotlinAndroidPluginId = libs.findPlugin("kotlin").get().get().pluginId
        val parcelizePluginId = libs.findPlugin("parcelize").get().get().pluginId
        val kotlinSerializationPluginId = libs.findPlugin("kotlin-serialization").get().get().pluginId

        pluginManager.apply(androidLibraryPluginId)
        pluginManager.apply(kotlinAndroidPluginId)
        pluginManager.apply(parcelizePluginId)
        pluginManager.apply(kotlinSerializationPluginId)

        configureAndroidLibraryDefaults()

        val kotlinxSerialization = libs.findLibrary("kotlinx-serialization").orNull
        val koinAndroid = libs.findLibrary("koin-android").orNull

        dependencies {
            kotlinxSerialization?.let { addProvider("implementation", it) }
            koinAndroid?.let { addProvider("implementation", it) }
        }
    }
}
