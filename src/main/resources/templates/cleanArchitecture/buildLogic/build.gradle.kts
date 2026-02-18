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
            id = "com.PACKAGE.convention.android.library.data"
            implementationClass = "com.PACKAGE.convention.DataConventionPlugin"
        }
        register("androidLibraryDIConvention") {
            id = "com.PACKAGE.convention.android.library.di"
            implementationClass = "com.PACKAGE.convention.DIConventionPlugin"
        }
        register("androidLibraryDomainConvention") {
            id = "com.PACKAGE.convention.android.library.domain"
            implementationClass = "com.PACKAGE.convention.DomainConventionPlugin"
        }
        register("androidLibraryPresentationConvention") {
            id = "com.PACKAGE.convention.android.library.presentation"
            implementationClass = "com.PACKAGE.convention.PresentationConventionPlugin"
        }
        register("androidLibraryDataSourceConvention") {
            id = "com.PACKAGE.convention.android.library.dataSource"
            implementationClass = "com.PACKAGE.convention.DataSourceConventionPlugin"
        }
        register("androidLibraryRemoteDataSourceConvention") {
            id = "com.PACKAGE.convention.android.library.remoteDataSource"
            implementationClass = "com.PACKAGE.convention.RemoteDataSourceConventionPlugin"
        }
        register("androidLibraryLocalDataSourceConvention") {
            id = "com.PACKAGE.convention.android.library.localDataSource"
            implementationClass = "com.PACKAGE.convention.LocalDataSourceConventionPlugin"
        }
    }
}
