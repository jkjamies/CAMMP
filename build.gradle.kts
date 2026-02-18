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
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.intelliJPlatform) apply false
    alias(libs.plugins.changelog) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }
}
