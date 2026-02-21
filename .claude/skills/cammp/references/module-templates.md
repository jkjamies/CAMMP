# CAMMP Module Templates Reference

## Module build.gradle.kts Template

All module build.gradle.kts files follow the same structure. Only the convention plugin alias and dependencies differ.

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

plugins {
    alias(libs.plugins.convention.android.library.{LAYER})
}

android {
    namespace = "{NAMESPACE}"
}

dependencies {
    // Add module-specific dependencies here as needed
    {DEPENDENCIES}
}
```

## Convention Plugin Alias Per Module

| Module | Plugin Alias |
|---|---|
| domain | `convention.android.library.domain` |
| data | `convention.android.library.data` |
| di | `convention.android.library.di` |
| presentation | `convention.android.library.presentation` |
| api | `convention.android.library.domain` (reuses domain) |
| dataSource | `convention.android.library.dataSource` |
| remoteDataSource | `convention.android.library.remoteDataSource` |
| localDataSource | `convention.android.library.localDataSource` |

## Inter-Module Dependency Graph

Dependencies are added as `implementation(project(...))` lines in the `dependencies` block.

| Module | Depends On |
|---|---|
| **domain** | `api` (only if `includeApiModule` is true) |
| **data** | `domain` |
| **di** | All other enabled modules except itself (domain, data, presentation, dataSource, etc.) |
| **presentation** | `api` (if `includeApiModule` is true), otherwise `domain` |
| **api** | (none) |
| **dataSource** | `data` |
| **remoteDataSource** | `data` |
| **localDataSource** | `data` |

**Example** for a feature `login` with root `app`, modules `domain`, `data`, `di`, `presentation`:

```kotlin
// domain/build.gradle.kts - no project dependencies (unless api module exists)

// data/build.gradle.kts
dependencies {
    implementation(project(":app:login:domain"))
}

// presentation/build.gradle.kts
dependencies {
    implementation(project(":app:login:domain"))
}

// di/build.gradle.kts
dependencies {
    implementation(project(":app:login:domain"))
    implementation(project(":app:login:data"))
    implementation(project(":app:login:presentation"))
}
```

## Directory Structure Per Module

### domain
```
domain/
├── build.gradle.kts
└── src/
    ├── main/kotlin/com/{org}/{root}/{feature}/domain/
    │   ├── Placeholder.kt
    │   ├── model/
    │   ├── repository/
    │   └── usecase/
    └── test/kotlin/
```

### data
```
data/
├── build.gradle.kts
└── src/
    ├── main/kotlin/com/{org}/{root}/{feature}/data/
    │   ├── Placeholder.kt
    │   ├── repository/
    │   └── (datasource subdirs based on strategy - see below)
    └── test/kotlin/
```

Datasource subdirectories in data module (based on datasource strategy):
- `none`: no subdirectories
- `combined`: `dataSource/`
- `local_only`: `localDataSource/`
- `remote_only`: `remoteDataSource/`
- `local_and_remote`: `remoteDataSource/` + `localDataSource/`

### api
```
api/
├── build.gradle.kts
└── src/
    ├── main/kotlin/com/{org}/{root}/{feature}/api/
    │   ├── Placeholder.kt
    │   ├── model/
    │   └── usecase/
    └── test/kotlin/
```

### di
```
di/
├── build.gradle.kts
└── src/
    ├── main/kotlin/com/{org}/{root}/{feature}/di/
    │   ├── Placeholder.kt
    │   ├── repository/
    │   └── usecase/
    └── test/kotlin/
```

If using `koin_annotations` DI strategy, an additional annotation module file is generated in the `di` package with `@Module` and `@ComponentScan` annotations targeting the feature's base package.

### presentation
```
presentation/
├── build.gradle.kts
└── src/
    ├── main/kotlin/com/{org}/{root}/{feature}/presentation/
    │   └── Placeholder.kt
    └── test/kotlin/
```

### dataSource / remoteDataSource / localDataSource
```
{dataSourceModule}/
├── build.gradle.kts
└── src/
    ├── main/kotlin/com/{org}/{root}/{feature}/{dataSourceModule}/
    │   └── Placeholder.kt
    └── test/kotlin/
```

## Placeholder.kt Template

```kotlin
package com.{sanitizedOrg}.{rootModule}.{featureName}.{moduleName}

class Placeholder
```

This is a minimal file to ensure the package exists and the module compiles. Users replace it with real code.