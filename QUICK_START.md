# NotablyMD Quick Start

## One-Time Setup
```bash
cd NotablyMD
chmod +x gradlew
java -version  # Should be JDK 21+ (NotablyMD uses Java 21)
```

## Daily Commands

### Start Development (Without Emulator)
```bash
# Run all tests and checks without emulator (fast, reliable)
./gradlew clean ktfmtFormat test lint check assembleDebug
```

### Start Development (With Emulator)
```bash
# Start emulator and run connected Android tests (waits for full boot, shuts down after)
export ANDROID_EMULATOR_WAIT_TIME_BEFORE_KILL=0 && (emulator -avd Pixel_7_API_33 -no-snapshot-load &) && adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done' && ./gradlew connectedAndroidTest; pkill -9 -f emulator
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
adb shell am start -n com.tsyche.notablymd.debug/com.tsyche.notablymd.presentation.activity.main.MainActivity

# Format code (required before commits)
./gradlew ktfmtFormat
```

### NotablyMD-Specific Commands
```bash
# Build release with obfuscation
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
/app/src/main/java/com/tsyche/notablymd/
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

## Key Diffs
- ✅ **Package**: `com.tsyche.notablymd`
- ✅ **Target SDK**: 35
- ✅ **Security**: SQLCipher encryption + biometrics
- ✅ **Markdown**: CommonMark + GFM support built-in
- ✅ **Import**: Evernote, Google Keep, JSON, Plain Text
- ✅ **Code Style**: ktfmt formatting (pre-commit hook)
- ✅ **Testing**: Comprehensive test coverage

## Essential Commands (Copy-Paste)
```bash
# Run tests without emulator (recommended for daily development)
./gradlew clean ktfmtFormat test lint check assembleDebug

# Start emulator and run connected tests (waits for full boot, shuts down after)
export ANDROID_EMULATOR_WAIT_TIME_BEFORE_KILL=0 && (emulator -avd Pixel_7_API_33 -no-snapshot-load &) && adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done' && ./gradlew connectedAndroidTest; pkill -9 -f emulator

# Markdown tests only
./gradlew test --tests "*markdown*" && ./gradlew connectedAndroidTest --tests "*markdown*"

# Quick build + install + launch
./gradlew assembleDebug installDebug && adb shell am start -n com.tsyche.notablymd.debug/com.tsyche.notablymd.presentation.activity.main.MainActivity

# Format code before commit
./gradlew ktfmtFormat
```

## Development Notes
- **Pre-commit hooks** automatically run ktfmtFormat
- **Tests** comprehensive
- **Security features** require additional permissions
- **Markdown import/export** already implemented
- **Biometric lock** available for app security
