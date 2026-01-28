# NotablyMD Quick Start

## One-Time Setup
```bash
cd NotablyMD
chmod +x gradlew
java -version  # Should be JDK 8+ (NotablyMD uses Java 8)
```

## Daily Commands

### Start Development
```bash
# Start emulator (API 36 for NotablyMD)
emulator -avd Pixel_7_API_36 -no-snapshot-load &

# Full development pipeline
./gradlew clean test connectedAndroidTest lint check assembleDebug installDebug && adb shell am start -n com.philkes.notablymd.debug/com.philkes.notablymd.presentation.activity.main.MainActivity
```

### Quick Commands
```bash
# Just build and install
./gradlew assembleDebug installDebug

# Just run tests
./gradlew test

# Run markdown-related tests
./gradlew test --tests "*markdown*"

# Launch app
adb shell am start -n com.philkes.notablymd.debug/com.philkes.notablymd.presentation.activity.main.MainActivity

# Format code (required before commits)
./gradlew ktfmtFormat
```

### NotablyMD-Specific Commands
```bash
# Build release with obfuscation
./gradlew assembleRelease

# Generate debug symbols for release builds
./gradlew assembleRelease

# Run all checks (matches GitHub Actions CI)
./gradlew test connectedAndroidTest lint check
```

### Troubleshooting
```bash
# Reset everything
./gradlew clean && rm -rf .gradle build && adb kill-server && adb start-server

# Check emulator
adb devices

# Check logs
adb logcat | grep notablymd

# Check crash reports
adb logcat | grep -E "(FATAL|AndroidRuntime)"
```

## File Structure
```
/app/src/main/java/com/philkes/notablymd/
├── data/                    # Database and models
├── presentation/            # UI components
│   ├── activity/           # Activities
│   ├── view/               # Custom views
│   └── viewmodel/          # ViewModels
├── utils/                  # Utilities and helpers
│   ├── security/           # Encryption, biometrics
│   ├── backup/             # Backup utilities
│   └── changehistory/      # Change tracking
└── data/imports/           # Import/export framework
    ├── markdown/            # Existing markdown support
    ├── evernote/            # Evernote importer
    └── google/              # Google Keep importer
```

## Key Differences from Original Notally
- ✅ **Package**: `com.philkes.notablymd` (not `com.omgodse.notally`)
- ✅ **Target SDK**: 36 (vs 35)
- ✅ **Security**: SQLCipher encryption + biometrics
- ✅ **Markdown**: CommonMark + GFM support built-in
- ✅ **Import**: Evernote, Google Keep, JSON, Plain Text
- ✅ **Code Style**: ktfmt formatting (pre-commit hook)
- ✅ **Testing**: Comprehensive test coverage

## Essential Commands (Copy-Paste)
```bash
# Start emulator + full pipeline
emulator -avd Pixel_7_API_36 -no-snapshot-load & && ./gradlew clean test connectedAndroidTest lint check assembleDebug installDebug && adb shell am start -n com.philkes.notablymd.debug/com.philkes.notablymd.presentation.activity.main.MainActivity

# Markdown tests only
./gradlew test --tests "*markdown*" && ./gradlew connectedAndroidTest --tests "*markdown*"

# Quick build + install + launch
./gradlew assembleDebug installDebug && adb shell am start -n com.philkes.notablymd.debug/com.philkes.notablymd.presentation.activity.main.MainActivity

# Format code before commit
./gradlew ktfmtFormat
```

## Development Notes
- **Pre-commit hooks** automatically run ktfmtFormat
- **Tests** are more comprehensive than original Notally
- **Security features** require additional permissions
- **Markdown import/export** already implemented
- **Biometric lock** available for app security
