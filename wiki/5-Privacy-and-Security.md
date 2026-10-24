# Privacy and security

Full texts: [privacy policy](https://github.com/Bosaj/android-update-checker/blob/main/docs/PRIVACY.md) · [security policy](https://github.com/Bosaj/android-update-checker/blob/main/SECURITY.md)

## Privacy by design
- **No `INTERNET` permission.** The app cannot send anything anywhere; no account, ads, analytics or crash reporting.
- **What it reads:** apps with a launcher icon (name, package, version, install and update dates, installer). Optionally, with Usage access, when each app was last used.
- **What it stores:** settings in DataStore, and the previous scan plus the updates feed in private files.
- **Backups:** settings only, and only to an end-to-end encrypted backup (`disableIfNoEncryptionCapabilities`).

## Permissions
| Permission | Granted | Why |
|---|---|---|
| `POST_NOTIFICATIONS` | runtime, optional | reminders |
| `PACKAGE_USAGE_STATS` | special access, optional, after an in-app disclosure | last-used dates |
| `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`, `ACCESS_NETWORK_STATE` | install | WorkManager reminder on Wi-Fi |

| Accessibility service (`BIND_ACCESSIBILITY_SERVICE`) | turned on by the user, optional, after an in-app disclosure | one-tap updates in Play Store and Galaxy Store only |

`FOREGROUND_SERVICE` and WorkManager's `SystemForegroundService` are removed in the manifest. No `QUERY_ALL_PACKAGES`.

## The one-tap helper
- Declared for `com.android.vending` and `com.sec.android.app.samsungapps` only, so Android delivers no events from other apps.
- Ignores every event unless the user started a run, and stops on Stop or at the end of the run.
- Reads button labels and view ids to find the store's own Update / "Update all" button; keeps only the list of apps the store showed as current.
- Fixed rules in `StorePage.kt` (unit-tested); no remote configuration, no AI, nothing downloaded.

## App hardening
- Exported components: the launcher activity, and AndroidX services and receivers that Android protects with `BIND_JOB_SERVICE` or `DUMP`.
- R8 minification and resource shrinking; verbose and debug logs stripped from release builds.
- APK signature v2 and v3 (the key can be rotated); signing keys never enter the repository.

## Supply chain
- Gradle wrapper pinned with a SHA-256 checksum and validated in CI (`gradle/actions/wrapper-validation`).
- Dependabot for Gradle dependencies and GitHub Actions, every week.
- CodeQL (`security-and-quality`) on every push, pull request and weekly.
- CI fails on any unit-test, lint or build error, and builds the release APK and App Bundle.

Report a vulnerability privately through [GitHub security advisories](https://github.com/Bosaj/android-update-checker/security/advisories/new).
