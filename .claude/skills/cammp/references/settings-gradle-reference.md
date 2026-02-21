# CAMMP Settings Gradle Reference

## Phase 1: Update settings.gradle.kts

### 1. Add includeBuild for build-logic

Ensure `settings.gradle.kts` at the project root contains:

```kotlin
includeBuild("build-logic")
```

Add this line if it doesn't already exist. Place it after any existing `pluginManagement` and `dependencyResolutionManagement` blocks.

### 2. Add module includes

For each enabled module, add an `include` line to `settings.gradle.kts`:

```kotlin
include(":{rootModule}:{featureDirName}:{moduleName}")
```

**Example** for feature `login` with root `app` and all modules enabled:

```kotlin
include(":app:login:domain")
include(":app:login:data")
include(":app:login:di")
include(":app:login:presentation")
```

With datasource strategy `local_and_remote`:
```kotlin
include(":app:login:domain")
include(":app:login:data")
include(":app:login:di")
include(":app:login:presentation")
include(":app:login:remoteDataSource")
include(":app:login:localDataSource")
```

Only add includes that don't already exist.

### 3. Update Version Catalog (gradle/libs.versions.toml)

Ensure the version catalog has plugin aliases for each convention plugin. Add to the `[plugins]` section:

```toml
[plugins]
# ... existing plugins ...
convention-android-library-domain = { id = "com.{sanitizedOrg}.convention.android.library.domain", version = "unspecified" }
convention-android-library-data = { id = "com.{sanitizedOrg}.convention.android.library.data", version = "unspecified" }
convention-android-library-di = { id = "com.{sanitizedOrg}.convention.android.library.di", version = "unspecified" }
convention-android-library-presentation = { id = "com.{sanitizedOrg}.convention.android.library.presentation", version = "unspecified" }
convention-android-library-dataSource = { id = "com.{sanitizedOrg}.convention.android.library.dataSource", version = "unspecified" }
convention-android-library-remoteDataSource = { id = "com.{sanitizedOrg}.convention.android.library.remoteDataSource", version = "unspecified" }
convention-android-library-localDataSource = { id = "com.{sanitizedOrg}.convention.android.library.localDataSource", version = "unspecified" }
```

Only add aliases for modules that are enabled. Only add if they don't already exist.

### 4. Add App-Level DI Dependency

Add the DI module as a dependency in the app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":{rootModule}:{featureDirName}:di"))
}
```

This wires the feature's DI module into the app's dependency graph. Only add if `includeDiModule` is true and the dependency doesn't already exist.

For Hilt projects, the app module should also have the Hilt plugin applied.