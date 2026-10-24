# Roadmap and versions

Update Radar follows [Semantic Versioning](https://semver.org/). Each version has a GitHub milestone, a `vX.Y.Z` tag and a release with the signed APK and its SHA-256.

| Version | Status | Highlights |
|---|---|---|
| [v0.1.0](https://github.com/Bosaj/android-update-checker/releases/tag/v0.1.0) | ✅ released | Repository foundation |
| [v1.0.0](https://github.com/Bosaj/android-update-checker/releases/tag/v1.0.0) | ✅ released | App inventory, staleness, Wi-Fi reminders, store deep links, review flow, recently-updated feed, English/French/Arabic |
| [v1.0.1](https://github.com/Bosaj/android-update-checker/releases/tag/v1.0.1) | ✅ released | Update all in Play Store, landscape scrolling |
| v1.1.0 | ✅ ready | Google Play ready (API 36), Check them all, last used, 8 languages with an in-app picker, privacy policy, licences, security hardening |
| v1.2.0 | 🗓 next | Home-screen widget; optional, opt-in source checks (F-Droid index, GitHub releases with ETag caching and SHA-256 checks) |

## Rules
- A fix is a PATCH, a backward-compatible feature is a MINOR, a breaking change is a MAJOR.
- Every issue belongs to the milestone of the version that ships it.
- `versionCode` goes up by one with every release.

## Ideas being weighed
- An in-app update prompt for Update Radar's own updates when installed from Google Play (Play In-App Updates API).
- Grouping the review by store, so Galaxy Store and Play Store apps come in two runs.

Open items live in the [issues](https://github.com/Bosaj/android-update-checker/issues) and [milestones](https://github.com/Bosaj/android-update-checker/milestones).
