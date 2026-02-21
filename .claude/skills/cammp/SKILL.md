---
name: cammp
description: Generate Clean Architecture module structure for Android/KMP projects (standalone)
argument-hint: "[feature-name]"
disable-model-invocation: true
---

# CAMMP Standalone Skill

Generate a full Clean Architecture module structure for an Android/KMP feature. This skill creates all the files directly - no MCP server required.

## Parameter Gathering

### Feature Name (required)
Use `$ARGUMENTS` as the feature name. If empty, ask the user what feature to generate.

Feature names support paths (e.g., `account/login`) and kebab-case (e.g., `user-profile`).

### Project Path (required)
Use the current working directory as the project root. Verify that `settings.gradle.kts` or `build.gradle.kts` exists there.

### Package Name (required)
Infer from existing project files:
1. Check `build.gradle.kts` or `build.gradle` in the app module for `namespace` or `applicationId`
2. Check `AndroidManifest.xml` for the `package` attribute
3. If not found, ask the user for their base package name (e.g., `com.example.app`)

### Root Module (optional, default: `app`)
Check `settings.gradle.kts` for the project's root module name. Common values: `app`, `shared`, `composeApp`.

### DI Strategy (optional, default: `hilt`)
Ask the user which DI framework to use, or infer from existing project dependencies. See [strategies reference](references/strategies.md) for details.

Options: `hilt`, `koin`, `koin_annotations`, `metro`

### Datasource Strategy (optional, default: `none`)
Ask the user what datasource structure they want. See [strategies reference](references/strategies.md) for details.

Options: `none`, `combined`, `local_only`, `remote_only`, `local_and_remote`

### Optional Modules
- **includePresentation** (default: `true`) - Presentation module with Compose UI
- **includeApiModule** (default: `false`) - API module (public contract layer)
- **includeDiModule** (default: `true`) - DI module

## Naming Conventions

See [naming conventions reference](references/naming-conventions.md) for full details on package name computation, namespace formatting, and settings include paths.

**Quick summary:**
- Sanitize the org from the package name: strip `com.`/`org.` prefix, remove invalid chars, default to `cammp`
- Convert feature name from kebab-case to camelCase: `user-profile` -> `userProfile`
- Package format: `com.{sanitizedOrg}.{rootModule}.{featureName}.{moduleName}`
- Settings include: `:{rootModule}:{feature}:{module}`

## Three-Phase Generation Pipeline

### Idempotency Rule
Before creating any file or directory, check if it already exists. Skip if present. Never overwrite existing files.

---

### Phase 1: Update settings.gradle.kts

See [settings gradle reference](references/settings-gradle-reference.md) for exact details.

1. Add `includeBuild("build-logic")` if not already present
2. Add `include` lines for each enabled module: `include(":{root}:{feature}:{module}")`
3. Ensure version catalog plugin aliases exist in `gradle/libs.versions.toml`
4. Add DI dependency to the app module's `build.gradle.kts` if needed

---

### Phase 2: Create build-logic

See [build logic reference](references/build-logic-reference.md) for exact templates and structure.

Create the `build-logic/` directory with:
1. `settings.gradle.kts` - Plugin management and version catalog from parent
2. `build.gradle.kts` - kotlin-dsl plugin, AGP/Kotlin deps, convention plugin registrations
3. Helper files in `src/main/kotlin/com/{sanitizedOrg}/convention/helpers/`:
   - `AndroidLibraryDefaults.kt`
   - `StandardTestDependencies.kt`
   - `TestOptions.kt`
4. Core files in `src/main/kotlin/com/{sanitizedOrg}/convention/core/`:
   - `Aliases.kt` - Version catalog alias constants
   - `Dependencies.kt` - DSL for adding dependencies
5. Convention plugins in `src/main/kotlin/com/{sanitizedOrg}/convention/`:
   - One plugin per enabled module type (Data, DI, Domain, Presentation, DataSource, etc.)

---

### Phase 3: Scaffold Feature Modules

See [module templates reference](references/module-templates.md) for exact build.gradle.kts templates, directory structures, and dependency graphs.

For each enabled module, create:
1. `{root}/{feature}/{module}/build.gradle.kts` - From template with namespace and dependencies
2. `{root}/{feature}/{module}/src/main/kotlin/{package/path}/Placeholder.kt`
3. `{root}/{feature}/{module}/src/test/kotlin/` (empty, for tests)
4. Module-specific subdirectories (e.g., `model/`, `repository/`, `usecase/` for domain)

---

## Post-Generation

After creating all files:
1. Summarize what was created and what was skipped
2. Remind the user to run a Gradle sync
3. Suggest next steps: implement domain models, repository interfaces, and use cases