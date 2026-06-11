package com.tsyche.notablymd.presentation.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Voice Recording Test Suite Runs all voice recording related tests including mock audio tests
 *
 * This suite includes:
 * - Critical regression tests (will fail if basic functionality breaks)
 * - Voice recording automation tests (UI workflow tests)
 * - Voice recording integration tests (mock audio integration)
 * - Unit tests (service and component tests)
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Critical regression tests - WILL FAIL if basic functionality is broken
    CriticalRegressionTests::class,

    // Voice recording automation tests - UI workflow tests
    VoiceRecordingAutomationTests::class,

    // Voice recording integration tests - Mock audio integration
    VoiceRecordingIntegrationTests::class,
)
class VoiceRecordingTestSuite {
    // Test suite class - no implementation needed
}

/** All Voice Recording Tests Suite Runs every voice recording test including unit tests */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CriticalRegressionTests::class,
    VoiceRecordingAutomationTests::class,
    VoiceRecordingIntegrationTests::class,
    // Unit tests would be added here if they were in the same test type
    // VoiceRecordingServiceUnitTest::class - This is a unit test, runs separately
)
class AllVoiceRecordingTests {
    // Test suite class - no implementation needed
}
