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

package com.jkjamies.cammp.domain.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

/**
 * Shared KotlinPoet [ClassName] and [MemberName] constants for annotations and types
 * referenced across multiple code generators. Centralising these avoids magic strings
 * and ensures consistency when the same third-party type is used by different factories.
 */
object GeneratedAnnotations {

    // ── Compose ────────────────────────────────────────────────────────
    val COMPOSABLE = ClassName("androidx.compose.runtime", "Composable")
    val REMEMBER = MemberName("androidx.compose.runtime", "remember")

    // ── Navigation ─────────────────────────────────────────────────────
    val NAV_HOST_CONTROLLER = ClassName("androidx.navigation", "NavHostController")
    val NAV_GRAPH_BUILDER = ClassName("androidx.navigation", "NavGraphBuilder")
    val NAV_HOST = MemberName("androidx.navigation.compose", "NavHost")
    val REMEMBER_NAV_CONTROLLER = MemberName("androidx.navigation.compose", "rememberNavController")
    val COMPOSABLE_DESTINATION = MemberName("androidx.navigation.compose", "composable")

    // ── Coroutines ─────────────────────────────────────────────────────
    val COROUTINE_SCOPE = ClassName("kotlinx.coroutines", "CoroutineScope")
    val REMEMBER_COROUTINE_SCOPE = MemberName("androidx.compose.runtime", "rememberCoroutineScope")

    // ── DI — Hilt / Dagger ─────────────────────────────────────────────
    val HILT_VIEW_MODEL = ClassName("dagger.hilt.android.lifecycle", "HiltViewModel")
    val JAVAX_INJECT = ClassName("javax.inject", "Inject")
    val DAGGER_BINDS = ClassName("dagger", "Binds")
    val DAGGER_MODULE = ClassName("dagger", "Module")
    val HILT_INSTALL_IN = ClassName("dagger.hilt", "InstallIn")
    val SINGLETON_COMPONENT = ClassName("dagger.hilt.components", "SingletonComponent")

    // ── DI — Koin ──────────────────────────────────────────────────────
    val KOIN_VIEW_MODEL = ClassName("org.koin.android.annotation", "KoinViewModel")
    val KOIN_SINGLE = ClassName("org.koin.core.annotation", "Single")
    val KOIN_MODULE = ClassName("org.koin.core.module", "Module")
    val KOIN_ANNOTATION_MODULE = ClassName("org.koin.core.annotation", "Module")
    val KOIN_COMPONENT_SCAN = ClassName("org.koin.core.annotation", "ComponentScan")

    // ── DI — Metro ──────────────────────────────────────────────────────
    val METRO_INJECT = ClassName("dev.zacsweers.metro", "Inject")
    val METRO_CONTRIBUTES_BINDING = ClassName("dev.zacsweers.metro", "ContributesBinding")
    val METRO_APP_SCOPE = ClassName("dev.zacsweers.metro", "AppScope")
    val METRO_CONTRIBUTES_INTO_MAP = ClassName("dev.zacsweers.metro", "ContributesIntoMap")
    val METRO_VIEW_MODEL_KEY = ClassName("dev.zacsweers.metro.viewmodel", "ViewModelKey")

    // ── DI — Compose integration ───────────────────────────────────────
    val HILT_VIEW_MODEL_COMPOSE = MemberName("androidx.hilt.lifecycle.viewmodel.compose", "hiltViewModel")
    val KOIN_VIEW_MODEL_COMPOSE = MemberName("org.koin.androidx.compose", "koinViewModel")
    val METRO_VIEW_MODEL_COMPOSE = MemberName("dev.zacsweers.metro.viewmodel.compose", "metroViewModel")

    // ── AndroidX Lifecycle ─────────────────────────────────────────────
    val VIEW_MODEL = ClassName("androidx.lifecycle", "ViewModel")

    // ── Serialization ──────────────────────────────────────────────────
    val SERIALIZABLE = ClassName("kotlinx.serialization", "Serializable")
}
