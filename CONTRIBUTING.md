# Contributing to Update Radar

Thanks for helping. Update Radar is a small, offline Android app; the rules below keep it honest, private and easy to release.

## Workflow
1. Branch from `main`: `feat/<short-name>`, `fix/<short-name>` or `docs/<short-name>`.
2. Commit with [Conventional Commits](https://www.conventionalcommits.org/): `type(scope): description` (`feat`, `fix`, `docs`, `build`, `ci`, `refactor`, `test`).
3. Open a pull request, fill in the template and link the issue (`Closes #N`). CI must be green: unit tests, lint, debug and release builds, CodeQL.

## Build and test
```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```
JDK 17 and Android SDK 36 are needed; Gradle downloads the SDK platform. On Windows, see the wiki page *Developer Setup* for the Gradle loopback fix.

## Rules
- **No `INTERNET` permission.** Anything online must be opt-in, explained in the UI and in `docs/PRIVACY.md`.
- **No `QUERY_ALL_PACKAGES`.** The launcher `<queries>` filter is enough.
- Be honest in the UI: the app finds and routes; the store does the update.
- Logic goes in `data/Radar.kt` (pure Kotlin) with unit tests; Android calls stay in the scanner, `UsageAccess` and `StoreLinks`.
- Never commit secrets: keystores and `keystore.properties` are git-ignored.

## Translations
The app ships in 8 languages: `values` (English), `values-fr`, `values-ar`, `values-es`, `values-de`, `values-it`, `values-pt-rBR`, `values-tr`.
- Every new string goes into all 8 files. Use plurals for counts, with the quantities each language needs (Arabic: zero, one, two, few, many, other).
- To add a language, add `values-xx/strings.xml`, then add it to `res/xml/locales_config.xml`, `LANGUAGES` in `SettingsScreen.kt`, `localeFilters` in `app/build.gradle.kts`, and a folder in `fastlane/metadata/android`.
- Check right-to-left layouts in Arabic before you open the PR.

## Versioning and releases
- [Semantic Versioning](https://semver.org/): a fix is a PATCH (`1.1.1`), a backward-compatible feature is a MINOR (`1.2.0`), a breaking change is a MAJOR.
- Tags are `vX.Y.Z`; GitHub milestones and releases use the same name. Every issue belongs to the milestone of the version that ships it.
- Each release raises `versionCode` by one and updates `versionName`, `CHANGELOG.md` and `fastlane/metadata/android/*/changelogs/<versionCode>.txt`.
- Release APKs are signed with the project key and published with their SHA-256.
