# NotablyMD Development Checklist

## Prerequisites

### Environment Setup
- [ ] **Java**: JDK 21+ (Temurin 21.0.2 via SDKMAN)
- [ ] **Android Studio**: Latest stable version
- [ ] **Gradle**: 8.11.1 (handled by wrapper)
- [ ] **Android SDK**: API 36+ (Pixel 7 API 36 emulator available)
- [ ] **Git**: For version control

### Initial Setup
```bash
# Clone repository (if not already done)
git clone <repository-url>
cd NotablyMD

# Verify Java version
java -version

# Make gradlew executable
chmod +x gradlew

# Install git hooks (auto-format)
./gradlew installLocalGitHooks
```

---

## Daily Development Workflow

### 1. Start Development Environment

#### Start Android Emulator
```bash
# List available emulators
emulator -list-avds

# Start Pixel 7 API 36 emulator (recommended)
emulator -avd Pixel_7_API_36 -no-snapshot-load

# Alternative: Start in background
emulator -avd Pixel_7_API_36 -no-snapshot-load &
```

#### Verify Emulator Status
```bash
# Check if emulator is running
adb devices

# Wait for boot completion (should show "Boot completed")
# Check emulator log for boot status
```

### 2. Build and Test Application

#### Clean Build
```bash
# Clean previous builds
./gradlew clean
```

#### Code Formatting (Required)
```bash
# Format code (required before commits)
./gradlew ktfmtFormat

# Check formatting without fixing
./gradlew ktfmtCheck
```

#### Build Application
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (when needed)
./gradlew assembleRelease

# Combined: Clean and build
./gradlew clean assembleDebug

# Combined: Clean, format, test, and build
./gradlew clean ktfmtFormat test assembleDebug

# Combined: Full development build (clean, format, test, lint, build)
./gradlew clean ktfmtFormat test lint assembleDebug
```

#### Run Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.tsyche.notablymd.data.imports.markdown.MarkdownUtilsTest"

# Run tests with coverage report
./gradlew testDebugUnitTest jacocoTestReport

# Combined: Run all markdown component tests
./gradlew test --tests "*markdown*"

# Combined: Run tests with detailed output
./gradlew test --info --tests "*markdown*"

# Test import/export functionality
./gradlew test --tests "*import*" --tests "*export*"
```

#### Run Integration Tests
```bash
# Run connected tests (requires emulator)
./gradlew connectedAndroidTest

# Run specific connected test
./gradlew connectedDebugAndroidTest --tests "com.tsyche.notablymd.ExampleTest"

# Combined: Run integration tests for markdown components
./gradlew connectedAndroidTest --tests "*markdown*"
```

#### Lint and Code Quality
```bash
# Run lint checks
./gradlew lint

# Run lint and fix auto-fixable issues
./gradlew lintFix

# Combined: Full code quality check (lint + format)
./gradlew lint lintFix ktfmtFormat
```

### 3. Install and Run Application

#### Install on Emulator
```bash
# Install debug APK
./gradlew installDebug

# Install release APK (when needed)
./gradlew installRelease

# Combined: Build and install debug APK
./gradlew assembleDebug installDebug

# Combined: Format, build, test, and install
./gradlew ktfmtFormat test assembleDebug installDebug
```

#### Launch Application
```bash
# Launch main activity
adb shell am start -n com.tsyche.notablymd.debug/com.tsyche.notablymd.presentation.activity.main.MainActivity

# Alternative: Launch using package name
adb shell monkey -p com.tsyche.notablymd.debug -c android.intent.category.LAUNCHER 1
```

#### Verify Installation
```bash
# Check installed packages
adb shell pm list packages | grep notablymd

# Check app version
adb shell dumpsys package com.tsyche.notablymd.debug | grep version

# Combined: Install and launch
./gradlew installDebug && adb shell am start -n com.tsyche.notablymd.debug/com.tsyche.notablymd.presentation.activity.main.MainActivity
```

---

## Testing Commands

### Unit Tests (Existing Components)
```bash
# Test all markdown components
./gradlew test --tests "*markdown*"

# Test specific existing components
./gradlew test --tests "MarkdownUtilsTest"
./gradlew test --tests "NotesImporterTest"
./gradlew test --tests "EvernoteImporterTest"
./gradlew test --tests "GoogleKeepImporterTest"

# Test security components
./gradlew test --tests "*security*"
./gradlew test --tests "*encryption*"

# Test backup components
./gradlew test --tests "*backup*"

# Combined: Run all existing tests with coverage
./gradlew test && ./gradlew jacocoTestReport

# Combined: Run tests with detailed output
./gradlew test --info --tests "*markdown*"
```

### Integration Tests
```bash
# Run all connected tests
./gradlew connectedAndroidTest

# Run specific connected test
./gradlew connectedDebugAndroidTest --tests "com.tsyche.notablymd.integration.*"

# Run instrumented tests for specific components
./gradlew connectedDebugAndroidTest --tests "*markdown*"

# Combined: Run all integration tests for markdown
./gradlew connectedAndroidTest --tests "*markdown*"
```

### Performance Tests
```bash
# Run performance benchmarks
./gradlew test --tests "*PerformanceTest"

# Combined: Build and profile
./gradlew assembleDebug && adb shell am start -n com.tsyche.notablymd.debug/.presentation.activity.main.MainActivity

# Monitor with: adb shell dumpsys meminfo com.tsyche.notablymd.debug
```

---

## Build Variants

### Debug Build (Development)
```bash
# Standard debug build
./gradlew assembleDebug

# Install debug build
./gradlew installDebug

# Debug build features:
# - Application ID suffix: .debug
# - Debuggable: true
# - Minify: false
# - Signing: debug keystore
```

### Release Build (Production)
```bash
# Release build (requires signing configuration)
./gradlew assembleRelease

# Install release build
./gradlew installRelease

# Release build features:
# - Application ID: com.tsyche.notablymd
# - Debuggable: false
# - Minify: true (ProGuard/R8)
# - Signing: release keystore (configure in app/build.gradle.kts)
```

---

## Troubleshooting Commands

### Common Issues

#### Emulator Problems
```bash
# Reset emulator
emulator -avd Pixel_7_API_36 -wipe-data

# Check emulator status
adb devices

# Restart ADB
adb kill-server && adb start-server

# Check emulator logs
adb logcat | grep -i notablymd
```

#### Build Issues
```bash
# Clean and rebuild
./gradlew clean build

# Clear Gradle cache
./gradlew clean
rm -rf .gradle
./gradlew build

# Check dependencies
./gradlew dependencies

# Check Gradle daemon status
./gradlew --status
```

#### Test Failures
```bash
# Run single test with debug info
./gradlew test --debug --tests "MarkdownUtilsTest.testParseBodyAndSpansFromMarkdown"

# Run tests with stacktrace
./gradlew test --stacktrace --tests "*markdown*"

# Check test reports
# View: app/build/reports/tests/testDebugUnitTest/index.html
```

#### Code Formatting Issues
```bash
# Fix formatting issues
./gradlew ktfmtFormat

# Check formatting without fixing
./gradlew ktfmtCheck

# Fix specific files
./gradlew ktfmtFormat --files="app/src/main/java/com/tsyche/notablymd/data/imports/markdown/MarkdownUtils.kt"
```

### Memory and Performance
```bash
# Monitor app memory usage
adb shell dumpsys meminfo com.tsyche.notablymd.debug

# Monitor CPU usage
adb shell top | grep notablymd

# Check app storage usage
adb shell dumpsys diskstats | grep notablymd

# Profile app startup time
adb shell am start -W -n com.tsyche.notablymd.debug/.presentation.activity.main.MainActivity
```

---

## Git Workflow Commands

### Daily Git Operations
```bash
# Check current status
git status

# Pull latest changes
git pull origin main

# Create feature branch
git checkout -b feature/markdown-sync-phase1

# Add and commit changes
git add .
git commit -m "Implement Phase 1: Enhanced markdown sync infrastructure"

# Push changes
git push origin feature/markdown-sync-phase1

# Merge to main (when ready)
git checkout main
git merge feature/markdown-sync-phase1
git push origin main
```

### Code Review Preparation
```bash
# Format code (required)
./gradlew ktfmtFormat

# Run full test suite
./gradlew test connectedAndroidTest lint

# Check for issues
./gradlew check

# Generate build report
./gradlew build
```

---

## Markdown Development Specific Commands

### Testing Existing Markdown Components
```bash
# Test existing CommonMark processing
./gradlew test --tests "MarkdownUtilsTest.testParseBodyAndSpansFromMarkdown"

# Test markdown export functionality
./gradlew test --tests "MarkdownUtilsTest.testCreateMarkdownFromBodyAndSpans"

# Test import framework
./gradlew test --tests "NotesImporterTest"
./gradlew test --tests "EvernoteImporterTest"
./gradlew test --tests "GoogleKeepImporterTest"

# Test security integration with markdown
./gradlew test --tests "*encryption*" --tests "*markdown*"
```

### Integration Testing Markdown Features
```bash
# Test markdown file operations on emulator
./gradlew connectedAndroidTest --tests "*markdown*"

# Test import/export with real files
adb shell mkdir -p /storage/emulated/0/Documents/NotablyMDTest
# Then run import/export tests

# Test CommonMark rendering
./gradlew connectedAndroidTest --tests "MarkdownIntegrationTest"
```

---

## Continuous Integration Commands

### Pre-push Checklist
```bash
# Combined: Full development pipeline
./gradlew clean ktfmtFormat test connectedAndroidTest lint check assembleDebug

# Combined: Install and verify
./gradlew installDebug && adb shell am start -n com.tsyche.notablymd.debug/com.tsyche.notablymd.presentation.activity.main.MainActivity

# Combined: Code quality and tests
./gradlew lint lintFix ktfmtFormat test connectedAndroidTest
```

### Automated Testing Script
```bash
#!/bin/bash
# run-full-checklist.sh

echo "🚀 Starting full development checklist..."

# Start emulator
echo "📱 Starting emulator..."
emulator -avd Pixel_7_API_36 -no-snapshot-load &
EMULATOR_PID=$!

# Wait for emulator boot
echo "⏳ Waiting for emulator boot..."
adb wait-for-device
adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done'
echo "✅ Emulator booted successfully"

# Format code
echo "🎨 Formatting code..."
./gradlew ktfmtFormat

# Clean build
echo "🧹 Cleaning build..."
./gradlew clean

# Run tests
echo "🧪 Running unit tests..."
./gradlew test

echo "🧪 Running integration tests..."
./gradlew connectedAndroidTest

# Run lint
echo "🔍 Running lint checks..."
./gradlew lint

# Build and install
echo "📦 Building and installing..."
./gradlew assembleDebug installDebug

# Launch app
echo "🚀 Launching application..."
adb shell am start -n com.tsyche.notablymd.debug/com.tsyche.notablymd.presentation.activity.main.MainActivity

echo "✅ Full checklist completed successfully!"

# Clean up
kill $EMULATOR_PID 2>/dev/null
```

---

## Quick Reference Commands

### Essential Commands (Copy-Paste Ready)
```bash
# Start emulator
emulator -avd Pixel_7_API_36 -no-snapshot-load &

# Combined: Format, clean, test, build, install
./gradlew ktfmtFormat clean test connectedAndroidTest assembleDebug installDebug

# Launch app
adb shell am start -n com.tsyche.notablymd.debug/com.tsyche.notablymd.presentation.activity.main.MainActivity

# Combined: Run markdown tests specifically
./gradlew test --tests "*markdown*" && ./gradlew connectedAndroidTest --tests "*markdown*"

# Combined: Check everything (full pipeline)
./gradlew clean ktfmtFormat test connectedAndroidTest lint check assembleDebug installDebug && adb shell am start -n com.tsyche.notablymd.debug/com.tsyche.notablymd.presentation.activity.main.MainActivity
```

### Development Environment Reset
```bash
# Complete environment reset
./gradlew clean
rm -rf .gradle build
adb kill-server && adb start-server
emulator -avd Pixel_7_API_36 -wipe-data &
```

---

## Notes

- **Emulator**: Pixel 7 API 36 is recommended for testing
- **Java Version**: Java 21 (Temurin 21.0.2 via SDKMAN)
- **Gradle**: Version 8.11.1 (handled by wrapper)
- **Code Formatting**: ktfmt with pre-commit hooks (required)
- **Tests**: Comprehensive test coverage for existing components
- **Build Time**: First build may take 2-3 minutes, subsequent builds are faster
- **Memory**: Emulator requires at least 4GB RAM, 8GB recommended for smooth development

### NotablyMD Specific Notes
- **Package**: `com.tsyche.notablymd`
- **CommonMark**: Version 0.27.0 with GFM extensions already integrated
- **Security**: SQLCipher encryption framework available
- **Import/Export**: Support for Evernote, Google Keep, JSON, Plain Text
- **Pre-commit**: Automatic code formatting with ktfmt

### Performance Tips
- Use `./gradlew --daemon` for faster builds
- Enable Gradle parallel execution in `gradle.properties`
- Keep emulator running for multiple test cycles
- Use `./gradlew assembleDebug` for quick builds during development
- Format code before commits to avoid pre-commit hook failures
