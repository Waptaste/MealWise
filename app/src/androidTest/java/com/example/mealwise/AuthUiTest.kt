package com.example.mealwise

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class AuthUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginScreen_rendersRequiredFields() {
        // Wait for splash to navigate to login
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasTestTag("email_field")).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithTag("email_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("navigate_to_register").assertIsDisplayed()
    }

    @Test
    fun loginValidation_showsErrors() {
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasTestTag("email_field")).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("login_button").performClick()
        
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password is required").assertIsDisplayed()
    }

    @Test
    fun navigateToRegister_showsRegisterFields() {
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasTestTag("navigate_to_register")).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("navigate_to_register").performClick()

        composeTestRule.onNodeWithTag("name_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("email_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirm_password_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("register_button").assertIsDisplayed()
    }

    @Test
    fun passwordMismatch_showsError() {
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasTestTag("navigate_to_register")).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("navigate_to_register").performClick()

        composeTestRule.onNodeWithTag("password_field").performTextInput("Password123")
        composeTestRule.onNodeWithTag("confirm_password_field").performTextInput("Different123")
        
        composeTestRule.onNodeWithTag("register_button").performClick()
        
        composeTestRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
    }
}
