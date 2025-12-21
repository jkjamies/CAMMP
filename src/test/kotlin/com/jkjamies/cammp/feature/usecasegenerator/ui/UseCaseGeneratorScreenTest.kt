package com.jkjamies.cammp.feature.usecasegenerator.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseGeneratorScreen
import com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseViewModel
import org.junit.Rule
import org.junit.Test

class UseCaseGeneratorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `screen renders correctly`() {
        val viewModel = UseCaseViewModel()
        
        composeTestRule.setContent {
            UseCaseGeneratorScreen(
                state = viewModel.state.value,
                onIntent = viewModel::handleIntent
            )
        }

        composeTestRule.onNodeWithText("Use Case Generator").assertExists()
    }
}
