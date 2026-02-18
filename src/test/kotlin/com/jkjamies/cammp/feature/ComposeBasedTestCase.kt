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

package com.jkjamies.cammp.feature

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.test.runTest
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.junit.Rule

/**
 * An abstract base class for Compose-based test cases.
 *
 * This class provides a framework for running Compose UI tests. It includes a
 * test rule for composing UI content and abstracts the content under test.
 */
internal abstract class ComposeBasedTestCase {
    @get:Rule
    val composableRule = createComposeRule()

    /**
     * Provides the Composable Content under test.
     */
    abstract val contentUnderTest: @Composable () -> Unit


    /**
     * Runs the given Compose test block in the context of a Compose content test rule.
     */
    fun runComposeTest(block: suspend ComposeTestRule.() -> Unit) = runTest {
        composableRule.setContentWrappedInTheme {
            contentUnderTest()
        }

        composableRule.block()
    }

    private fun ComposeContentTestRule.setContentWrappedInTheme(content: @Composable () -> Unit) {
        setContent {
            IntUiTheme {
                content()
            }
        }
    }
}