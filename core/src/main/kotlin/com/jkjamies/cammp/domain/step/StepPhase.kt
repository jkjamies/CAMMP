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

package com.jkjamies.cammp.domain.step

/**
 * Declares the execution phase of a generation step.
 *
 * Steps within a generator are sorted by phase ordinal before execution.
 * Steps sharing the same phase are inherently independent of each other.
 */
enum class StepPhase {
    /** Create module directories, build files, and source skeletons. */
    SCAFFOLD,

    /** Generate source files (interfaces, implementations, UI components). */
    GENERATE,

    /** Update settings, build-logic, or other project configuration. */
    CONFIGURE,

    /** Wire dependency injection modules and bindings. */
    DI,
}
