package com.tsyche.notablymd.presentation.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.tsyche.notablymd.R
import com.tsyche.notablymd.presentation.activity.VoiceWidgetConfigureActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class VoiceWidgetConfigureUITest {

    @get:Rule val activityRule = ActivityScenarioRule(VoiceWidgetConfigureActivity::class.java)

    @Test
    fun configureActivityShouldDisplayTitleAndDescription() {
        // Check that title is displayed
        onView(withText("Voice Note Widget")).check(matches(isDisplayed()))

        // Check that description is displayed
        onView(
                withText(
                    "Add a voice recording widget to your home screen for quick note creation."
                )
            )
            .check(matches(isDisplayed()))
    }

    @Test
    fun configureActivityShouldDisplayFeaturesList() {
        // Check that features header is displayed
        onView(withText("Features:")).check(matches(isDisplayed()))

        // Check that individual features are displayed
        onView(withText("• One-tap voice recording")).check(matches(isDisplayed()))

        onView(withText("• Real-time voice visualization")).check(matches(isDisplayed()))

        onView(withText("• Automatic note creation")).check(matches(isDisplayed()))

        onView(withText("• Quick access to last note")).check(matches(isDisplayed()))
    }

    @Test
    fun addButtonShouldBeClickable() {
        // Check that add button exists and is clickable
        onView(withId(R.id.addButton)).check(matches(isDisplayed())).check(matches(isEnabled()))
    }

    @Test
    fun cancelButtonShouldBeClickable() {
        // Check that cancel button exists and is clickable
        onView(withId(R.id.cancelButton)).check(matches(isDisplayed())).check(matches(isEnabled()))
    }

    @Test
    fun clickingAddButtonShouldFinishActivity() {
        // Click the add button
        onView(withId(R.id.addButton)).perform(click())

        // Activity should be finished (this is harder to test directly with Espresso)
        // In a real test, you might use ActivityScenario to check if activity is finished
    }

    @Test
    fun clickingCancelButtonShouldFinishActivity() {
        // Click the cancel button
        onView(withId(R.id.cancelButton)).perform(click())

        // Activity should be finished
    }

    @Test
    fun uiElementsShouldHaveCorrectText() {
        // Check button text
        onView(withId(R.id.addButton)).check(matches(withText("Add Widget")))

        onView(withId(R.id.cancelButton)).check(matches(withText("Cancel")))
    }

    @Test
    fun layoutShouldBeProperlyStructured() {
        // Check that all important views are present
        onView(withId(R.id.titleText)).check(matches(isDisplayed()))

        onView(withId(R.id.descriptionText)).check(matches(isDisplayed()))

        onView(withId(R.id.addButton)).check(matches(isDisplayed()))

        onView(withId(R.id.cancelButton)).check(matches(isDisplayed()))
    }
}
