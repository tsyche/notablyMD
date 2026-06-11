package com.tsyche.notablymd.presentation.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Comprehensive UI Test Suite for Voice Recording Features Runs all UI tests to verify voice
 * recording functionality works in emulator
 *
 * This suite tests:
 * - Main app navigation and voice recording access
 * - Quick Record Triggers settings and configuration
 * - Widget configuration and customization
 * - Voice Recording Service functionality
 * - Quick Settings Tile integration
 * - Widget configuration activity
 *
 * Note: Physical button combinations (Power+Volume Up) cannot be tested in emulator but all other
 * voice recording triggers should work
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    VoiceRecordingUITest::class,
    QuickRecordTriggersUITest::class,
    WidgetConfigurationUITest::class,
    VoiceRecordingServiceUITest::class,
    QuickSettingsTileUITest::class,
    VoiceWidgetConfigureUITest::class,
)
class VoiceRecordingUITestSuite {
    // Test suite class - no implementation needed
}
