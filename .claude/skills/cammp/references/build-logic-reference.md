# CAMMP Build Logic Reference

## Overview

The `build-logic/` directory is an included build that provides convention plugins for configuring Android library modules. All files are idempotent - only create if they don't already exist.

## Directory Structure

```
build-logic/
├── settings.gradle.kts
├── build.gradle.kts
└── src/main/kotlin/com/{sanitizedOrg}/convention/
    ├── helpers/
    │   ├── AndroidLibraryDefaults.kt
    │   ├── StandardTestDependencies.kt
    │   └── TestOptions.kt
    ├── core/
    │   ├── Aliases.kt
    │   └── Dependencies.kt
    ├── DataConventionPlugin.kt
    ├── DIConventionPlugin.kt
    ├── DomainConventionPlugin.kt
    ├── PresentationConventionPlugin.kt
    ├── DataSourceConventionPlugin.kt        (if combined strategy)
    ├── RemoteDataSourceConventionPlugin.kt  (if remote strategy)
    └── LocalDataSourceConventionPlugin.kt   (if local strategy)
```

## build-logic/settings.gradle.kts

```kotlin
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

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
```

## build-logic/build.gradle.kts

Replace `{PACKAGE}` with `{sanitizedOrg}` throughout:

```kotlin
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

import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    `kotlin-dsl`
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val agpVersion = libs.findVersion("agp").get().requiredVersion
val kotlinVersion = libs.findVersion("kotlin").get().requiredVersion

dependencies {
    implementation("com.android.tools.build:gradle:$agpVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}

gradlePlugin {
    plugins {
        register("androidLibraryDataConvention") {
            id = "com.{sanitizedOrg}.convention.android.library.data"
            implementationClass = "com.{sanitizedOrg}.convention.DataConventionPlugin"
        }
        register("androidLibraryDIConvention") {
            id = "com.{sanitizedOrg}.convention.android.library.di"
            implementationClass = "com.{sanitizedOrg}.convention.DIConventionPlugin"
        }
        register("androidLibraryDomainConvention") {
            id = "com.{sanitizedOrg}.convention.android.library.domain"
            implementationClass = "com.{sanitizedOrg}.convention.DomainConventionPlugin"
        }
        register("androidLibraryPresentationConvention") {
            id = "com.{sanitizedOrg}.convention.android.library.presentation"
            implementationClass = "com.{sanitizedOrg}.convention.PresentationConventionPlugin"
        }
        register("androidLibraryDataSourceConvention") {
            id = "com.{sanitizedOrg}.convention.android.library.dataSource"
            implementationClass = "com.{sanitizedOrg}.convention.DataSourceConventionPlugin"
        }
        register("androidLibraryRemoteDataSourceConvention") {
            id = "com.{sanitizedOrg}.convention.android.library.remoteDataSource"
            implementationClass = "com.{sanitizedOrg}.convention.RemoteDataSourceConventionPlugin"
        }
        register("androidLibraryLocalDataSourceConvention") {
            id = "com.{sanitizedOrg}.convention.android.library.localDataSource"
            implementationClass = "com.{sanitizedOrg}.convention.LocalDataSourceConventionPlugin"
        }
    }
}
```

## Helper Files

### helpers/AndroidLibraryDefaults.kt

Configures standard Android library settings. Replace `{PACKAGE}` with `{sanitizedOrg}`:

```kotlin
package com.{sanitizedOrg}.convention.helpers

// Configures:
// - compileSdk, minSdk, targetSdk from gradle.properties (with defaults 35, 28)
// - defaultConfig: testInstrumentationRunner, useLibrary("android.test.mock")
// - buildFeatures.buildConfig = true
// - Unit test options with JUnit Platform
// - Kotlin jvmToolchain(17)
// - Standard test dependencies
```

The function `configureAndroidLibraryDefaults()` is called by every convention plugin.

### helpers/StandardTestDependencies.kt

Adds standard test dependencies:
- `kotest-runner-junit5`
- `kotest-assertions-core`
- `kotest-property`
- `junit-vintage-engine`
- `mockk`
- `kotlinx-coroutines-test`
- `turbine`

### helpers/TestOptions.kt

Configures JUnit Platform test execution, JVM arguments for module access, and Jacoco per-test coverage.

## Core Files

### core/Aliases.kt

Contains version catalog alias constants organized as:

```kotlin
internal object Aliases {
    internal object Operations { /* LIBS, IMPLEMENTATION, KSP, etc. */ }
    internal object PluginAliases { /* ANDROID_LIBRARY, KOTLIN_ANDROID, etc. */ }
    internal object BuildPropAliases { /* COMPILE_SDK, MIN_SDK, etc. */ }
    internal object Dependencies {
        internal object LibsCommon { /* KOTLINX_SERIALIZATION, JSON, CORE_KTX, + DI-specific */ }
        internal object LibsCompose { /* COMPOSE_BOM, UI, MATERIAL3_ANDROID, NAVIGATION, etc. */ }
        internal object LibsCoroutines { /* CORE, ANDROID */ }
        internal object LibsUnitTest { /* KOTEST_RUNNER, MOCKK, TURBINE, etc. */ }
        internal object LibsAndroidTest { /* ANDROIDX_TEST_RUNNER, ESPRESSO, etc. */ }
    }
}
```

The actual alias values are resolved from the project's `gradle/libs.versions.toml`. If a library/plugin already exists in the catalog, its existing alias is used. Otherwise, a new entry is added with a default alias name.

### core/Dependencies.kt

Provides a DSL for adding dependencies from the version catalog:

```kotlin
package com.{sanitizedOrg}.convention.core

// Provides:
// - libsCatalog() extension to get version catalog
// - propInt() / propString() for gradle.properties access
// - pluginId() for resolving plugin IDs from catalog
// - Dependencies DSL class with: implementation, ksp, implementationPlatform,
//   debugImplementation, testImplementation, androidTestImplementation
// - Plugins DSL class with: apply(alias) for applying catalog plugins
```

## Convention Plugin Structure

Each convention plugin follows the same pattern. Here's the general structure:

```kotlin
package com.{sanitizedOrg}.convention

import org.gradle.api.Plugin
import org.gradle.api.Project

internal class {Name}ConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libsCatalog = // ... get version catalog

            // 1. Apply plugins (Android Library, Kotlin, DI-specific, Parcelize, Serialization)
            // 2. Configure Android library defaults
            // 3. Add dependencies (common, DI-specific, coroutines)
            // 4. Add test dependencies
        }
    }
}
```

### Plugin Variations by DI Strategy

**Common plugins applied by all strategies:**
- `com.android.library`
- `org.jetbrains.kotlin.android`
- `org.jetbrains.kotlin.plugin.parcelize`
- `org.jetbrains.kotlin.plugin.serialization`

**Additional plugins by DI strategy:**

| DI Strategy | Additional Plugins |
|---|---|
| hilt | `com.google.devtools.ksp`, `com.google.dagger.hilt.android` |
| koin | (none) |
| koin_annotations | `com.google.devtools.ksp` |
| metro | `dev.zacsweers.metro` |

**DI-specific dependencies:**

| DI Strategy | Dependencies |
|---|---|
| hilt | `hilt-android` (impl), `hilt-android-compiler` (ksp) |
| koin | `koin-android` (impl) |
| koin_annotations | `koin-core` (impl), `koin-annotations` (impl), `koin-ksp-compiler` (ksp) |
| metro | (none - Metro is a compiler plugin) |

### Presentation Convention Plugin

In addition to common configuration, the Presentation plugin:
- Applies `org.jetbrains.kotlin.plugin.compose`
- Enables `buildFeatures.compose = true`
- Adds Compose dependencies: BOM, UI, Graphics, Material3, Navigation, Tooling, Preview
- Adds DI-specific navigation dependency (hilt-navigation-compose, koin-androidx-compose, metro-viewmodel-compose)
- Adds Compose-specific test dependencies (ui-test-junit4, ui-test-manifest)

### DataSource Convention Plugins

DataSource, RemoteDataSource, and LocalDataSource plugins follow the same pattern as the Data plugin:
- Apply common plugins
- Configure Android defaults
- Add common and DI-specific dependencies
- Add coroutines and test dependencies