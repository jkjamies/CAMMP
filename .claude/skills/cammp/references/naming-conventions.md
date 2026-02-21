# CAMMP Naming Conventions Reference

## Org Sanitization Algorithm

Given a `packageName` input (e.g., `com.example.app`):

1. Trim whitespace
2. If wrapped in `${}`, unwrap it
3. Strip leading `com.` or `org.` prefix
4. Remove any characters that are NOT `A-Z`, `a-z`, `0-9`, `_`, or `.`
5. Trim leading/trailing `.` characters
6. If result is blank, default to `cammp`
7. Lowercase the first character

**Examples:**
| Input | sanitizedOrg |
|---|---|
| `com.example.app` | `example.app` |
| `com.mycompany.project` | `mycompany.project` |
| `org.myorg.app` | `myorg.app` |
| `com.My-Company!.app` | `myCompany.app` |
| `` (empty) | `cammp` |

## Feature Name Conversion

Feature names in kebab-case are converted to camelCase for use in package names:

1. Split on `-` (hyphen)
2. First segment stays lowercase
3. Subsequent segments get their first character capitalized
4. Join all segments together
5. Ensure first character is lowercase

**Examples:**
| Input | featureName (camelCase) |
|---|---|
| `login` | `login` |
| `user-profile` | `userProfile` |
| `account-settings-page` | `accountSettingsPage` |

The original feature name (with hyphens) is used as the **directory name** (`featureDirName`).

## Package Name Format

```
com.{sanitizedOrg}.{rootModule}.{featureName}.{moduleName}
```

**Example** with `packageName=com.example.app`, `rootModule=app`, `feature=user-profile`, `module=domain`:
```
com.example.app.userProfile.domain
```

## Namespace Format

The namespace in `build.gradle.kts` uses the same format as the package name:
```
com.{sanitizedOrg}.{rootModule}.{featureName}.{moduleName}
```

## Settings Include Path Format

```
:{rootModule}:{featureDirName}:{moduleName}
```

**Example** with `rootModule=app`, `feature=user-profile`:
```
:app:user-profile:domain
:app:user-profile:data
:app:user-profile:di
:app:user-profile:presentation
```

Note: The feature directory name preserves the original input (kebab-case), while the package name uses camelCase.

## Project Dependency Path Format

Used in `implementation(project(...))` within build.gradle.kts files:

```
:{rootModule}:{featureDirName}:{moduleName}
```

Root module segments separated by `/` are split into colon-separated path segments:
- Root `app` -> `:app:feature:module`
- Root `shared/core` -> `:shared:core:feature:module`

## Convention Plugin ID Format

```
com.{sanitizedOrg}.convention.android.library.{layer}
```

**Layers:** `domain`, `data`, `di`, `presentation`, `dataSource`, `remoteDataSource`, `localDataSource`

**Example** with `packageName=com.example.app`:
```
com.example.app.convention.android.library.domain
com.example.app.convention.android.library.data
com.example.app.convention.android.library.di
```

## Convention Plugin Class Names

| Module | Plugin Class Name |
|---|---|
| domain | `DomainConventionPlugin` |
| data | `DataConventionPlugin` |
| di | `DIConventionPlugin` |
| presentation | `PresentationConventionPlugin` |
| dataSource | `DataSourceConventionPlugin` |
| remoteDataSource | `RemoteDataSourceConventionPlugin` |
| localDataSource | `LocalDataSourceConventionPlugin` |

## Placeholder File Format

Each module gets a `Placeholder.kt` at its package root:

```kotlin
package com.{sanitizedOrg}.{rootModule}.{featureName}.{moduleName}

class Placeholder
```