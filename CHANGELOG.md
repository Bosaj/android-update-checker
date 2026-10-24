# Changelog

All notable changes to this project are documented here. Format: [Keep a Changelog](https://keepachangelog.com/), versioning: [SemVer](https://semver.org/).

## [Unreleased]

## [1.0.1] - 2026-10-24
### Added
- **Update all in Play Store**: a button on the home screen opens Play Store's "Manage apps & device" Updates page, where its own "Update all" button updates every app at once, so you do not tap 200 store pages one by one. Opening the page also makes Play Store check for new versions. With Galaxy Store installed, a second button opens Galaxy Store (it has no public link to its Updates page: Menu › Updates › Update all).

### Fixed
- Landscape: the review screen and the app detail sheet scroll, so their buttons are no longer cut off on short screens (found on a Galaxy S23+).

## [1.0.0] - 2026-10-24
### Added
- Update Radar app (Kotlin, Jetpack Compose, Material 3): radar of installed apps by days since their last update, fresh / aging / stale thresholds, filters, search and sorting.
- Installer detection for Play Store, Galaxy Store, AppGallery, Amazon Appstore, F-Droid, other stores and apps installed by hand; one tap to the store page with web fallbacks.
- Review flow for stale apps, "recently updated" feed built from scan diffs, ignore list.
- WorkManager reminders on unmetered networks (every 1, 3 or 7 days, optionally only while charging).
- Arabic (RTL), French and English; light and dark themes; README screenshots in `docs/`.
- Signed release build (keystore outside the repository, read from `keystore.properties` or `UR_*` variables).

### Fixed
- Onboarding no longer flashes on every cold start: the splash screen stays until settings are read.
- Placeholder ROM dates (before 2010) show as "Factory version, never updated".
- Arabic: the stale count was measured too narrow and wrapped; digits are now consistent with the UI language, and counts use brackets because the Arabic zero (٠) looks like the "·" separator.
- Radar blips no longer cover the centre count; the detail sheet opens fully so its buttons clear the navigation bar.
- Theme: chips and sliders use the teal palette instead of the Material default lavender.
- "All done. 1 apps reviewed" is now a proper plural in all three languages.

### CI
- Unit tests, Android Lint and the debug build now fail the job (no `|| true`); the debug APK is uploaded as an artifact.

### Added (scaffold)
- Initial repository scaffold: README (with architecture, roadmap and an LLM-handoff section), LICENSE, .gitignore, CONTRIBUTING, CODE_OF_CONDUCT, SECURITY, issue/PR templates, and wiki pages.
