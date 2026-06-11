# NotablyMD — Android (Kotlin/Gradle) task runner

# List all available recipes
default:
    @just --list

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

# Sync CLAUDE.md and AGENTS.md — copies the newer file to the older one
sync-docs:
    @if [ CLAUDE.md -nt AGENTS.md ]; then cp CLAUDE.md AGENTS.md; echo "Synced CLAUDE.md -> AGENTS.md"; elif [ AGENTS.md -nt CLAUDE.md ]; then cp AGENTS.md CLAUDE.md; echo "Synced AGENTS.md -> CLAUDE.md"; else echo "CLAUDE.md and AGENTS.md are in sync"; fi
