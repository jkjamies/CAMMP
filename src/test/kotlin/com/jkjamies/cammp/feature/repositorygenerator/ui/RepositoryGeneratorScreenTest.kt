package com.jkjamies.cammp.feature.repositorygenerator.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryGeneratorScreen
import com.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryViewModel
import org.junit.Rule
import org.junit.Test

class RepositoryGeneratorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `screen renders correctly`() {
        val viewModel = RepositoryViewModel()
        
        composeTestRule.setContent {
            RepositoryGeneratorScreen(
                state = viewModel.state.value,
                onIntent = viewModel::handleIntent
            )
        }

        composeTestRule.onNodeWithText("Repository Generator").assertExists()
    }
}
