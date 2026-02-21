# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CAMMP (Clean Architecture Multi-Module Plugin) is an IntelliJ plugin + MCP server + Claude Code skills that automates Clean Architecture scaffolding for Android/KMP projects. It generates fully wired module structures with dependency injection, testing harnesses, and Gradle configurations. Published on the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/29447).

### Multi-Module Architecture

The project is split into three Gradle modules:

- **`:core`** - Pure Kotlin shared code (Clean Architecture generator, domain models, KotlinPoet factories). No IntelliJ dependencies. Uses `java-library` + `java-test-fixtures` plugins. Exposes `api(kotlinpoet)` and `api(coroutines-core)` for downstream modules. Metro DI classes must NOT be `internal` (cross-module wiring).
- **`:plugin`** - IntelliJ plugin (UI, toolwindow, IDE-specific generators). Depends on `implementation(project(":core"))` + `testFixtures(project(":core"))`.
- **`:mcp-server`** - MCP (Model Context Protocol) server for AI-assisted scaffolding. Communicates via stdio transport, packaged as a shadow JAR (`cammp-mcp.jar`). Uses `createGraph<McpGraph>()` for Metro DI.

## Build & Development Commands

```bash
# Build all modules
./gradlew build

# Run all tests (Kotest + JUnit, concurrent execution)
./gradlew test

# Run a single test class
./gradlew :core:test --tests "*CleanArchitectureGeneratorTest"
./gradlew :plugin:test --tests "*GenerateModulesViewModelTest"
./gradlew :mcp-server:test --tests "*GenerateFeatureToolTest"

# Code coverage report (Kover, XML output)
./gradlew koverXmlReport

# Run the plugin in a sandboxed IDE instance
./gradlew :plugin:runIde

# Verify plugin compatibility
./gradlew :plugin:verifyPlugin

# Build MCP server shadow JAR
./gradlew :mcp-server:shadowJar
# Output: mcp-server/build/libs/cammp-mcp.jar
```

**Note:** JVM toolchain is 21. Gradle 9.2.1 with configuration cache and build cache enabled. Tests use JUnit Platform runner with `kotest.framework.config.fqn=com.jkjamies.cammp.ProjectConfig` for concurrent spec/test execution.

## Architecture

### Key Paths

- Core: `core/src/main/kotlin/com/jkjamies/cammp/`
- Plugin: `plugin/src/main/kotlin/com/jkjamies/cammp/`
- MCP Server: `mcp-server/src/main/kotlin/com/jkjamies/cammp/mcp/`
- Version catalog: `gradle/libs.versions.toml`
- Plugin manifest: `plugin/src/main/resources/META-INF/plugin.xml`

### Feature Vertical Slices

Each feature is a self-contained vertical slice following Clean Architecture. Core features (shared between plugin and MCP server) live in `:core`, while plugin-specific features live in `:plugin`:

**`:core` features** (under `core/src/main/kotlin/com/jkjamies/cammp/feature/`):
- **cleanarchitecture** - Full module generation (settings.gradle, build.gradle, all layers)

**`:plugin` features** (under `plugin/src/main/kotlin/com/jkjamies/cammp/feature/`):
- **repositorygenerator** - Repository interface + implementation generation
- **usecasegenerator** - Use Case scaffolding
- **presentationgenerator** - MVI presentation layer generation
- **welcome** - Onboarding UI

Each feature contains: `domain/`, `data/`, `datasource/`, `presentation/`, and `di/` subdirectories.

### Layer Dependency Rules

- **domain**: Pure Kotlin. DEPENDS ON NOTHING. Contains entities, repository interfaces, use cases, sealed result types.
- **data**: Implements domain interfaces. Contains repository implementations, KotlinPoet SpecFactories, mappers. Depends on domain + datasource.
- **datasource**: Low-level I/O (FileSystem, IntelliJ PSI, Gradle). No business logic.
- **presentation**: MVI pattern (ViewModels, Intents, UiState). Depends on domain only. NEVER depends on data.

### Step Pattern (Generation Pipelines)

Code generation uses an ordered pipeline of Steps contributed via Metro DI:

1. Each generator feature defines a sealed `StepResult` and a `Step` interface with a `phase: StepPhase` and `suspend fun execute(params): StepResult`
2. Steps are contributed to the DI graph via `@ContributesIntoSet(AppScope::class)`
3. A Generator class (`@Inject`) receives `Set<Step>`, sorts by phase, and executes sequentially
4. Example: `CleanArchitectureGenerator` runs steps in phase order: SETTINGS → BUILD_LOGIC → SCAFFOLD

To add a new generation step: create a class implementing the feature's Step interface, annotate with `@ContributesIntoSet(AppScope::class)`, and assign the appropriate `StepPhase`.

### Metro Dependency Injection

- Plugin graph: `plugin/src/main/kotlin/com/jkjamies/cammp/di/CammpGraph.kt` (`@DependencyGraph(AppScope::class)`)
- MCP graph: `mcp-server/src/main/kotlin/com/jkjamies/cammp/mcp/di/McpGraph.kt` (`@DependencyGraph(AppScope::class)`)
- Each feature contributes via `@ContributesTo(AppScope::class)` interfaces in its `di/` package
- Steps contributed via `@ContributesIntoSet`, consumed as `Set<Step>` by generators
- Each graph entry point provides its own `CoroutineDispatcher` (Dispatchers.Default)

### MCP Server

The `:mcp-server` module exposes CAMMP's Clean Architecture generator via the [Model Context Protocol](https://modelcontextprotocol.io/):

- **Transport**: stdio (stdin/stdout JSON-RPC)
- **Tool**: `generate_feature` - Creates a full Clean Architecture module structure with configurable DI strategy, datasource strategy, and optional modules (presentation, API, DI)
- **Resources**: `cammp://strategies/di` and `cammp://strategies/datasource` - Markdown descriptions of available strategies for AI client discoverability
- **Packaging**: Shadow JAR (`./gradlew :mcp-server:shadowJar` → `cammp-mcp.jar`)
- **Usage**: `java -jar cammp-mcp.jar` (runs as stdio MCP server)

### Claude Code Skills

CAMMP provides two Claude Code skills in `.claude/skills/` for generating Clean Architecture modules directly:

- **`/cammp-mcp`** (`.claude/skills/cammp-mcp/SKILL.md`) - Delegates to the CAMMP MCP server's `generate_feature` tool. Requires MCP server configured.
- **`/cammp`** (`.claude/skills/cammp/SKILL.md`) - Standalone skill that generates files directly. References in `.claude/skills/cammp/references/` provide strategy docs, naming conventions, module templates, settings.gradle instructions, and build-logic scaffolding details.

Skills are packaged as zip artifacts (`cammp-skill.zip`, `cammp-mcp-skill.zip`) in CI builds and releases.

### MVI Pattern

All presentation logic follows strict MVI/UDF:
- **UiState**: Immutable data class with `val` properties, updated via `.copy()`
- **Intents**: Sealed interface representing user actions
- **ViewModel**: Exposes single `StateFlow<UiState>`, processes Intents via `handleIntent()`
- **Side Effects**: One-off events via `Channel` or `SharedFlow`, never State

## Coding Standards

- **Copyright Headers**: All `.kt` and `.kts` files MUST include Apache 2.0 header with `Copyright 2025-2026 Jason Jamieson`
- **Immutability**: `val` everywhere. No mutable collections in public APIs.
- **Exhaustive `when`**: Always use `when` with sealed types as statements to force compile errors on new cases.
- **Functional style**: Prefer `map`, `filter`, `fold` over loops.
- **Coroutines**: Always inject `CoroutineDispatcher`. Use `StateFlow` for state, `SharedFlow` for events.

## Testing Standards

- **Framework**: Kotest BDD-style `BehaviorSpec` (Given/When/Then) with concurrent execution
- **Fakes > Mocks**: Use in-memory fakes for repositories and data sources. MockK only for IntelliJ SDK interfaces.
- **Flow testing**: Use Turbine (`flow.test { ... }`) with `StandardTestDispatcher` and `advanceUntilIdle()`
- **Coverage**: Kover with exclusions for DI-generated code, Compose UI (pending compose test rules), and IntelliJ VFS wiring
- Tests mirror source structure: `src/test/kotlin/com/jkjamies/cammp/feature/`

## Tech Stack

- **Kotlin** 2.3.0+ / JVM 21
- **IntelliJ Platform SDK** (2024.3.6, Community Edition)
- **Compose Desktop** with Jewel theme for plugin UI
- **Metro** (`dev.zacsweers.metro`) for compile-time DI
- **KotlinPoet** for code generation
- **MCP Kotlin SDK** (`io.modelcontextprotocol:sdk`) for MCP server
- **Shadow** plugin for MCP server fat JAR packaging
- **Kotest** 6.x, **MockK**, **Turbine** for testing
- **Kover** for code coverage
- Version catalog: `gradle/libs.versions.toml`