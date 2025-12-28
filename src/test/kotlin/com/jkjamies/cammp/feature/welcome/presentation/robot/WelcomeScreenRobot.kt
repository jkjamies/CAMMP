package com.jkjamies.cammp.feature.welcome.presentation.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo

internal class WelcomeScreenRobot(private val composableRule: ComposeTestRule) {

    fun verifyTextIsDisplayed(text: String) {
        composableRule
            .onNodeWithText(text)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun verifyTextContains(text: String) {
        composableRule
            .onNodeWithText(text, substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun clickOnTag(testTag: String) {
        composableRule
            .onNodeWithTag(testTag)
            .performScrollTo()
            .performClick()
    }
}
