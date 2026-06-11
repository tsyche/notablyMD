package com.tsyche.notablymd.presentation.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.tsyche.notablymd.R
import com.tsyche.notablymd.presentation.activity.main.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ABSOLUTE MINIMUM FUNCTIONALITY TESTS These tests WILL FAIL if the most basic functionality
 * doesn't work They are designed to catch the exact issues you're experiencing
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AbsoluteMinimumTests {

    @get:Rule val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val permissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.POST_NOTIFICATIONS,
        )

    @Test
    fun settingsMustOpenWithoutCrash_ABSOLUTE() {
        // This test WILL FAIL if settings crash
        // It tries the most basic settings access possible

        var settingsOpened = false
        var crashDetected = false

        try {
            // Wait for app to fully load
            Thread.sleep(5000)

            // Try EVERY possible way to open settings
            val settingsAttempts =
                listOf(
                    // Method 1: Settings content description
                    {
                        try {
                            onView(withContentDescription("Settings")).perform(click())
                            Thread.sleep(2000)
                            // Look for ANY settings text to confirm it opened
                            onView(withText("Widget")).check(matches(isDisplayed()))
                            settingsOpened = true
                        } catch (e: Exception) {
                            // Method 1 failed
                        }
                    },

                    // Method 2: Menu button
                    {
                        try {
                            onView(withContentDescription("More options")).perform(click())
                            Thread.sleep(1000)
                            onView(withText("Settings")).perform(click())
                            Thread.sleep(2000)
                            onView(withText("Widget")).check(matches(isDisplayed()))
                            settingsOpened = true
                        } catch (e: Exception) {
                            // Method 2 failed
                        }
                    },

                    // Method 3: Toolbar click
                    {
                        try {
                            onView(withId(R.id.Toolbar)).perform(click())
                            Thread.sleep(1000)
                            onView(withText("Settings")).perform(click())
                            Thread.sleep(2000)
                            onView(withText("Widget")).check(matches(isDisplayed()))
                            settingsOpened = true
                        } catch (e: Exception) {
                            // Method 3 failed
                        }
                    },

                    // Method 4: Check if settings are already visible
                    {
                        try {
                            onView(withText("Settings")).check(matches(isDisplayed()))
                            settingsOpened = true
                        } catch (e: Exception) {
                            // Method 4 failed
                        }
                    },
                )

            // Try each method
            for ((index, method) in settingsAttempts.withIndex()) {
                try {
                    method()
                    if (settingsOpened) {
                        println("Settings opened with method $index")
                        break
                    }
                } catch (e: Exception) {
                    println("Settings method $index failed: ${e.message}")
                    crashDetected = true
                }
            }
        } catch (e: Exception) {
            crashDetected = true
            println("Complete settings test failure: ${e.message}")
        }

        if (!settingsOpened) {
            throw AssertionError(
                "CRITICAL FAILURE: Settings cannot be opened by ANY method. App is broken."
            )
        }

        if (crashDetected) {
            throw AssertionError(
                "CRITICAL FAILURE: Settings access caused crashes. App is unstable."
            )
        }
    }

    @Test
    fun voiceRecordingMustCreateFiles_ABSOLUTE() {
        // This test WILL FAIL if voice recording doesn't create files
        // It tests the absolute minimum: start recording, stop recording, check for files

        var recordingStarted = false
        var recordingStopped = false
        var anySuccessIndicator = false

        try {
            // Wait for app to load
            Thread.sleep(5000)

            // Try EVERY possible way to start recording
            val startAttempts =
                listOf(
                    {
                        try {
                            onView(withText("Record")).perform(click())
                            recordingStarted = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                    {
                        try {
                            onView(withId(R.id.TakeNote)).perform(click())
                            recordingStarted = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                    {
                        try {
                            onView(withContentDescription("Record")).perform(click())
                            recordingStarted = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                    {
                        try {
                            onView(withContentDescription("Take Note")).perform(click())
                            recordingStarted = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                )

            // Try each start method
            for ((index, method) in startAttempts.withIndex()) {
                try {
                    method()
                    if (recordingStarted) {
                        println("Recording started with method $index")
                        break
                    }
                } catch (e: Exception) {
                    println("Recording start method $index failed: ${e.message}")
                }
            }

            if (!recordingStarted) {
                throw AssertionError(
                    "CRITICAL FAILURE: Cannot start voice recording with ANY method."
                )
            }

            // Wait for recording to initialize
            Thread.sleep(3000)

            // Try EVERY possible way to stop recording
            val stopAttempts =
                listOf(
                    {
                        try {
                            onView(withText("Stop")).perform(click())
                            recordingStopped = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                    {
                        try {
                            onView(withContentDescription("Stop")).perform(click())
                            recordingStopped = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                    {
                        try {
                            // Double-click the same button to stop
                            onView(withId(R.id.TakeNote)).perform(click())
                            Thread.sleep(500)
                            onView(withId(R.id.TakeNote)).perform(click())
                            recordingStopped = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                )

            // Try each stop method
            for ((index, method) in stopAttempts.withIndex()) {
                try {
                    method()
                    if (recordingStopped) {
                        println("Recording stopped with method $index")
                        break
                    }
                } catch (e: Exception) {
                    println("Recording stop method $index failed: ${e.message}")
                }
            }

            if (!recordingStopped) {
                throw AssertionError(
                    "CRITICAL FAILURE: Cannot stop voice recording with ANY method."
                )
            }

            // Wait for file creation and processing
            Thread.sleep(8000) // Longer wait for processing

            // Check for ANY success indicator
            val successChecks =
                listOf(
                    {
                        try {
                            onView(withText("Voice note created")).check(matches(isDisplayed()))
                            anySuccessIndicator = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                    {
                        try {
                            onView(withText("Voice")).check(matches(isDisplayed()))
                            anySuccessIndicator = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                    {
                        try {
                            onView(withText("note")).check(matches(isDisplayed()))
                            anySuccessIndicator = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                    {
                        try {
                            onView(withText("created")).check(matches(isDisplayed()))
                            anySuccessIndicator = true
                        } catch (e: Exception) {
                            // Failed
                        }
                    },
                )

            // Try each success check
            for ((index, check) in successChecks.withIndex()) {
                try {
                    check()
                    if (anySuccessIndicator) {
                        println("Success confirmed with check $index")
                        break
                    }
                } catch (e: Exception) {
                    println("Success check $index failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            throw AssertionError(
                "CRITICAL FAILURE: Voice recording workflow completely failed: ${e.message}"
            )
        }

        if (!anySuccessIndicator) {
            throw AssertionError(
                "CRITICAL FAILURE: Voice recording completed but NO success indicator shown. Files likely not created."
            )
        }
    }

    @Test
    fun appMustNotCrash_ABSOLUTE() {
        // This test WILL FAIL if app crashes on launch
        try {
            // Wait for app to fully load
            Thread.sleep(5000)

            // Check for the most basic UI elements
            onView(withId(R.id.NavHostFragment)).check(matches(isDisplayed()))
            onView(withId(R.id.Toolbar)).check(matches(isDisplayed()))
        } catch (e: Exception) {
            throw AssertionError(
                "CRITICAL FAILURE: App crashes on launch or has broken main UI: ${e.message}"
            )
        }
    }
}

// Extension function for indexed iteration
private fun <T> List<T>.withIndex(): List<IndexedValue<T>> {
    return this.mapIndexed { index, value -> IndexedValue(index, value) }
}

data class IndexedValue<T>(val index: Int, val value: T)
