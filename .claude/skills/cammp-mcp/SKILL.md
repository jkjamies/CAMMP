---
name: cammp-mcp
description: Generate Clean Architecture module structure using the CAMMP MCP server
argument-hint: "[feature-name]"
disable-model-invocation: true
---

# CAMMP MCP Skill

Generate a Clean Architecture module structure for an Android/KMP feature using the CAMMP MCP server's `generate_feature` tool.

## Setup Check

First, verify the `generate_feature` MCP tool is available. If it is NOT available, tell the user:

> The CAMMP MCP server is not configured. Either:
> 1. Add it to your MCP config: `java -jar cammp-mcp.jar` via stdio transport
> 2. Use `/cammp` instead (standalone skill, no MCP server required)

If the tool IS available, proceed with parameter gathering.

## Parameter Gathering

### Feature Name (required)
Use `$ARGUMENTS` as the feature name. If empty, ask the user what feature to generate.

Feature names support paths (e.g., `account/login`) and kebab-case (e.g., `user-profile`).

### Project Path (required)
Infer from the current working directory. This should be the absolute path to the Android/KMP project root (where `settings.gradle.kts` or `build.gradle.kts` lives).

### Package Name (required)
Infer from existing project files:
1. Check `build.gradle.kts` or `build.gradle` in the app module for `namespace` or `applicationId`
2. Check `AndroidManifest.xml` for the `package` attribute
3. If not found, ask the user for their base package name (e.g., `com.example.app`)

### Root Module (optional, default: `app`)
Check `settings.gradle.kts` for the project's root module name. Common values: `app`, `shared`, `composeApp`.

### DI Strategy (optional, default: `hilt`)
Ask the user which DI framework to use, or infer from existing project dependencies:

- **hilt** (default) - Google's Hilt for Android. Generates `@Module`, `@InstallIn`, `@Binds` annotations. Requires Hilt Gradle plugin.
- **koin** - Koin DI framework. Generates Koin module definitions using DSL. Lightweight, no code generation.
- **koin_annotations** - Koin with KSP annotation processing. Generates `@Module`, `@Single`, `@Factory` annotations. Requires koin-annotations KSP plugin.
- **metro** - Metro compile-time DI (by Zac Sweers). Generates `@DependencyGraph`, `@ContributesTo`, `@Inject` annotations. Zero runtime reflection.

### Datasource Strategy (optional, default: `none`)
Ask the user what datasource structure they want:

- **none** (default) - No separate datasource module. Data layer handles all data access directly.
- **combined** - Single `dataSource` module for all data sources. Good for simple features with one data source.
- **local_only** - Generates a `localDataSource` module. For features that only need local storage (Room, DataStore, etc.).
- **remote_only** - Generates a `remoteDataSource` module. For features that only need network access (Retrofit, Ktor, etc.).
- **local_and_remote** - Generates both `localDataSource` and `remoteDataSource` modules. For features that need both local caching and remote API access.

### Optional Modules
- **includePresentation** (default: `true`) - Whether to include a presentation module with Compose UI scaffolding
- **includeApiModule** (default: `false`) - Whether to include an API module (public contract layer)
- **includeDiModule** (default: `true`) - Whether to include a DI module

## Tool Call

Call the `generate_feature` MCP tool with the gathered parameters:

```json
{
  "featureName": "<feature-name>",
  "projectPath": "<absolute-path-to-project-root>",
  "packageName": "<base-package-name>",
  "rootModule": "<root-module>",
  "diStrategy": "<hilt|koin|koin_annotations|metro>",
  "datasourceStrategy": "<none|combined|local_only|remote_only|local_and_remote>",
  "includePresentation": true,
  "includeApiModule": false,
  "includeDiModule": true
}
```

## Post-Generation

After the tool returns successfully:
1. Summarize what was created (modules, build files, convention plugins)
2. Note any skipped modules (already existed)
3. Remind the user to run a Gradle sync to pick up the new modules
4. Suggest next steps: implement domain models, repository interfaces, and use cases
