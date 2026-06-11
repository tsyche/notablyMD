# NotablyMD

Android markdown-first note-taking app (Kotlin, Gradle, minSdk 21, targetSdk 35).

## Build & dev commands

Uses `just` as task runner — run `just --list` for all recipes.

| Command | What it does |
|---------|-------------|
| `just build` | Debug APK |
| `just install` | Build + install on device |
| `just test` | Unit tests |
| `just check` | Full check (format + test + lint + build) |
| `just lintfix` | Auto-format with ktfmt |
| `just release` | Release AAB for Play Store |

## Key files

- `app/build.gradle.kts` — app config, SDK versions, signing
- `gradle.properties` — build flags (no secrets)
- `local.properties` — machine-local config + signing passwords (gitignored)
- `fastlane/Fastfile` — Play Store deployment lanes

## Notes

- Signing credentials live in `local.properties` (gitignored) — not in `gradle.properties`
- Java 21 (Temurin) via SDKMAN — see `local.properties` for `org.gradle.java.home`
- ktfmt is enforced — run `just lintfix` before committing
