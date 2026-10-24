# Security Policy

## Supported versions

Only the latest minor release gets security fixes. Versions follow [Semantic Versioning](https://semver.org/).

| Version | Supported |
|---|---|
| 1.2.x | ✅ |
| 1.1.x | ❌ — please update to 1.2 |
| 1.0.x | ❌ |
| < 1.0 | ❌ |

## Reporting a vulnerability

- **Do not** open a public issue for anything exploitable.
- Report it privately with [GitHub security advisories](https://github.com/Bosaj/android-update-checker/security/advisories/new) ("Report a vulnerability").
- Include the app version (Settings › About), the Android version and device, steps to reproduce, and the impact.
- You will get a reply within 7 days. A confirmed issue is fixed in a patch release (`1.1.x`) and credited in the CHANGELOG unless you prefer otherwise.

## How the app is built to be safe

- **No `INTERNET` permission.** The app cannot send data anywhere. See the [privacy policy](docs/PRIVACY.md).
- **Few permissions.** Only notifications (optional), plus what WorkManager needs to run on Wi-Fi. The foreground-service permission and service that WorkManager would merge in are removed.
- **A narrowly scoped accessibility helper (optional).** It is declared for Play Store and Galaxy Store only (`android:packageNames`), ignores every event unless the user started a run, follows a fixed rule (press the store's own Update or "Update all" button), and is off until the user turns it on after an in-app disclosure.
- **Narrow package visibility.** Only the launcher-app query is declared; no `QUERY_ALL_PACKAGES`.
- **Nothing exported that needs no permission.** The only exported components are the launcher activity and AndroidX components that Android protects with system permissions (`BIND_JOB_SERVICE`, `DUMP`).
- **Encrypted backups only.** Cloud backup contains settings only, and only when the backup is end-to-end encrypted.
- **Release builds:** R8 minification and resource shrinking, debug logs stripped, APK signature schemes v2 and v3 (so the key can be rotated).
- **Signing keys never touch the repository.** They are read from a git-ignored `keystore.properties` or from `UR_*` environment variables.

## Supply chain

- The Gradle wrapper pins the distribution with a SHA-256 checksum, and CI validates the wrapper JAR.
- Dependabot proposes updates for Gradle dependencies and GitHub Actions every week.
- CodeQL scans the Kotlin code on every push and pull request.
- CI fails on any unit-test, lint or build error.

Never commit secrets. Keys belong in git-ignored files (`keystore.properties`, `.env`); see `.env.example`.
