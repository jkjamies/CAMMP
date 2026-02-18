# CAMMP Guidelines

This file provides guidance to JetBrains Junie when working with code in this repository.

## Project Overview

CAMMP (Clean Architecture Multi-Module Plugin) is an IntelliJ plugin that automates Clean Architecture scaffolding for Android/KMP projects. It generates fully wired module structures with dependency injection, testing harnesses, and Gradle configurations. Published on the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/29447) (v0.0.8-alpha).

## Build & Development Commands

```bash
# Build the plugin
./gradlew build

# Run all tests (Kotest + JUnit, concurrent execution)
./gradlew test

# Run a single test class
./gradlew test --tests "com.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesViewModelTest"

# Run a single test by full path pattern
./gradlew test --tests "*GenerateModulesViewModelTest"

# Code coverage report (Kover, XML output)
./gradlew koverXmlReport

# Run the plugin in a sandboxed IDE instance
./gradlew runIde

# Verify plugin compatibility
./gradlew verifyPlugin
```

**Note:** JVM toolchain is 21. Gradle 9.2.1 with configuration cache and build cache enabled. Tests use JUnit Platform runner with `kotest.framework.config.fqn=com.jkjamies.cammp.ProjectConfig` for concurrent spec/test execution.

## Architecture

### Feature Vertical Slices

Each feature under `src/main/kotlin/com/jkjamies/cammp/feature/` is a self-contained vertical slice following Clean Architecture:

- **cleanarchitecture** - Full module generation (settings.gradle, build.gradle, all layers)
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

- Root graph: `src/main/kotlin/com/jkjamies/cammp/di/CammpGraph.kt` (`@DependencyGraph(AppScope::class)`)
- Each feature contributes via `@ContributesTo(AppScope::class)` interfaces in its `di/` package
- Steps contributed via `@ContributesIntoSet`, consumed as `Set<Step>` by generators
- `CoroutineModule` provides `CoroutineDispatcher` (Dispatchers.Default)

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
- **Kotest** 6.x, **MockK**, **Turbine** for testing
- **Kover** for code coverage
- Version catalog: `gradle/libs.versions.toml`