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

package com.jkjamies.cammp.mcp.resources

val DI_STRATEGIES_TEXT = """
    # Dependency Injection Strategies

    CAMMP supports the following DI frameworks for generated modules:

    ## hilt (default)
    - Google's Hilt for Android
    - Generates @Module, @InstallIn, @Binds annotations
    - Requires Hilt Gradle plugin in consumer project

    ## koin
    - Koin DI framework
    - Generates Koin module definitions using DSL
    - Lightweight, no code generation

    ## koin_annotations
    - Koin with KSP annotation processing
    - Generates @Module, @Single, @Factory annotations
    - Requires koin-annotations KSP plugin

    ## metro
    - Metro compile-time DI (by Zac Sweers)
    - Generates @DependencyGraph, @ContributesTo, @Inject annotations
    - Zero runtime reflection
""".trimIndent()

val DATASOURCE_STRATEGIES_TEXT = """
    # Datasource Strategies

    Controls which datasource modules are generated alongside domain and data:

    ## none (default)
    - No separate datasource module
    - Data layer handles all data access directly

    ## combined
    - Single 'dataSource' module for all data sources
    - Good for simple features with one data source

    ## local_only
    - Generates a 'localDataSource' module
    - For features that only need local storage (Room, DataStore, etc.)

    ## remote_only
    - Generates a 'remoteDataSource' module
    - For features that only need network access (Retrofit, Ktor, etc.)

    ## local_and_remote
    - Generates both 'localDataSource' and 'remoteDataSource' modules
    - For features that need both local caching and remote API access
""".trimIndent()
