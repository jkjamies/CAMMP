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
