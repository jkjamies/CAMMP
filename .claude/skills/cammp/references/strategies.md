# CAMMP Strategies Reference

## Dependency Injection Strategies

CAMMP supports the following DI frameworks for generated modules:

### hilt (default)
- Google's Hilt for Android
- Generates `@Module`, `@InstallIn`, `@Binds` annotations
- Requires Hilt Gradle plugin in consumer project
- Convention plugins apply: `com.android.library`, `org.jetbrains.kotlin.android`, KSP, Hilt, Parcelize, Kotlin Serialization
- Adds dependencies: `hilt-android`, `hilt-android-compiler` (KSP), `hilt-navigation-compose` (presentation)
- **When to choose**: Standard choice for most Android projects. Mature ecosystem, strong IDE support, compile-time validation.

### koin
- Koin DI framework
- Generates Koin module definitions using DSL
- Lightweight, no code generation
- Convention plugins apply: `com.android.library`, `org.jetbrains.kotlin.android`, Parcelize, Kotlin Serialization
- Adds dependencies: `koin-android`, `koin-androidx-compose` (presentation)
- **When to choose**: Simpler projects, KMP targets, or teams preferring DSL-based DI without annotation processing.

### koin_annotations
- Koin with KSP annotation processing
- Generates `@Module`, `@Single`, `@Factory` annotations
- Requires koin-annotations KSP plugin
- Convention plugins apply: `com.android.library`, `org.jetbrains.kotlin.android`, KSP, Parcelize, Kotlin Serialization
- Adds dependencies: `koin-core`, `koin-annotations`, `koin-ksp-compiler` (KSP), `koin-androidx-compose` (presentation)
- Additionally generates a Koin annotation module file in the DI module with `@ComponentScan`
- **When to choose**: Teams using Koin who want compile-time safety via annotations instead of DSL.

### metro
- Metro compile-time DI (by Zac Sweers)
- Generates `@DependencyGraph`, `@ContributesTo`, `@Inject` annotations
- Zero runtime reflection
- Convention plugins apply: `com.android.library`, `org.jetbrains.kotlin.android`, Metro, Parcelize, Kotlin Serialization
- Adds dependencies: `metrox-viewmodel-compose` (presentation)
- **When to choose**: Projects prioritizing compile-time DI with zero runtime overhead. Modern alternative to Dagger/Hilt.

---

## Datasource Strategies

Controls which datasource modules are generated alongside domain and data:

### none (default)
- No separate datasource module
- Data layer handles all data access directly
- Modules created: `domain`, `data` (+ optional `di`, `presentation`, `api`)
- **When to choose**: Simple features where data access is straightforward and doesn't need abstraction layers.

### combined
- Single `dataSource` module for all data sources
- Good for simple features with one data source
- Modules created: `domain`, `data`, `dataSource` (+ optional `di`, `presentation`, `api`)
- The `data` module gets a `dataSource/` subdirectory in its package
- The `dataSource` module depends on `data` via `implementation(project(...))`
- **When to choose**: Features with a single data source type (e.g., only API calls or only local DB).

### local_only
- Generates a `localDataSource` module
- For features that only need local storage (Room, DataStore, etc.)
- Modules created: `domain`, `data`, `localDataSource` (+ optional `di`, `presentation`, `api`)
- The `data` module gets a `localDataSource/` subdirectory
- The `localDataSource` module depends on `data`
- **When to choose**: Features backed entirely by local storage.

### remote_only
- Generates a `remoteDataSource` module
- For features that only need network access (Retrofit, Ktor, etc.)
- Modules created: `domain`, `data`, `remoteDataSource` (+ optional `di`, `presentation`, `api`)
- The `data` module gets a `remoteDataSource/` subdirectory
- The `remoteDataSource` module depends on `data`
- **When to choose**: Features backed entirely by network APIs.

### local_and_remote
- Generates both `localDataSource` and `remoteDataSource` modules
- For features that need both local caching and remote API access
- Modules created: `domain`, `data`, `remoteDataSource`, `localDataSource` (+ optional `di`, `presentation`, `api`)
- The `data` module gets both `remoteDataSource/` and `localDataSource/` subdirectories
- Both datasource modules depend on `data`
- **When to choose**: Features with offline-first architecture or local caching of remote data.
