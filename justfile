# NotablyMD — Android (Kotlin/Gradle) task runner

# Build debug APK
build:
    ./gradlew assembleDebug

# Build and install debug APK on connected device
install:
    ./gradlew assembleDebug installDebug

# Run unit tests
test:
    ./gradlew test

# Run connected Android tests (requires running emulator or device)
connected:
    ./gradlew connectedAndroidTest

# Check formatting and lint
lint:
    ./gradlew ktfmtCheck lint

# Auto-fix formatting
lintfix:
    ./gradlew ktfmtFormat

# Full check: format, test, lint, build (no emulator)
check:
    ./gradlew clean ktfmtFormat test lint check assembleDebug

# Build release AAB for Play Store
release:
    ./gradlew clean bundleRelease

# Remove build artifacts
clean:
    ./gradlew clean

# Clean everything and reinstall (nuke and rebuild)
fresh: clean build
