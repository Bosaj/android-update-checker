# 1. Architecture

1. Inventory: PackageManager + installer source (Play, Galaxy Store, sideload) + version + lastUpdateTime.
2. Staleness: days since last update per app, with thresholds and a "recently updated" feed from scan diffs.
3. Reminders: WorkManager periodic job on unmetered Wi-Fi (optionally while charging).
4. Actions: deep links to the store page that installed each app; batch "review stale apps" flow.

## Proposed stack
- Kotlin + Jetpack Compose (Material 3)
- WorkManager
- DataStore
- No network permission needed for v1

## Open bases to fork
- [ImranR98/Obtainium](https://github.com/ImranR98/Obtainium) — Gets updates directly from source (GitHub, F-Droid); complementary for sideloaded apps.
- [Play In-App Updates](https://developer.android.com/guide/playcore/in-app-updates) — Works only for your own app — confirms the platform limit.
- [WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent) — Background reminders constrained to unmetered networks.
