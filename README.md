# Update Radar

> See which Android apps have gone stale, get a Wi-Fi reminder, and jump straight to the right store page to update — Play Store and Galaxy Store.

**Status:** pre-alpha scaffold (no code yet). **Feasibility rating:** 8/10 — a real daily annoyance on Galaxy phones; Android limits what any app can force.
**Idea:** #20 in the owner's ideas backlog. **Author:** Oussama EL HADJI. Part of the Bosaj ideas portfolio (sibling repo: `dual_portfolio_hub`).

---

## Problem

On Galaxy phones some apps do not update for weeks: auto-update is off, Galaxy Store and Play Store split the apps, or update notifications arrive late. The user notices only when something breaks.

## Why build it (gap analysis)

Android does not let a normal app trigger Play Store or Galaxy Store updates for other apps, and there is no public API that says "update available" for another app. What an app can do: list installed apps with their installer and last-update date, flag the stale ones, remind on unmetered Wi-Fi, and open each app's page in the store that installed it. Rooting is not a default option.

## Prior art & open bases to fork

- [ImranR98/Obtainium](https://github.com/ImranR98/Obtainium) — Gets updates directly from source (GitHub, F-Droid); complementary for sideloaded apps.
- [Play In-App Updates](https://developer.android.com/guide/playcore/in-app-updates) — Works only for your own app — confirms the platform limit.
- [WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent) — Background reminders constrained to unmetered networks.

## Proposed architecture

1. Inventory: PackageManager + installer source (Play, Galaxy Store, sideload) + version + lastUpdateTime.
2. Staleness: days since last update per app, with thresholds and a "recently updated" feed from scan diffs.
3. Reminders: WorkManager periodic job on unmetered Wi-Fi (optionally while charging).
4. Actions: deep links to the store page that installed each app; batch "review stale apps" flow.

## Tech stack (proposed)

- Kotlin + Jetpack Compose (Material 3)
- WorkManager
- DataStore
- No network permission needed for v1

## Roadmap

- v0.1 — Installed-app inventory with installer and staleness.
- v0.2 — Wi-Fi reminders with WorkManager.
- v0.3 — Store deep links + review flow + recently-updated feed.
- v0.4 — Optional source checks (F-Droid / GitHub) and widgets.

---

## For LLM agents & contributors continuing this project

This repo is a **scaffold**: README + standard files, no implementation yet. If you are an AI agent asked to "continue developing", read this section first.

- Be honest in the UI: the app cannot force updates; it finds and routes.
- Keep v1 offline: no INTERNET permission, so privacy is easy to prove.

**Ground rules:** keep this README in sync with reality as you build; prefer forking/reusing the bases above over greenfield code; write tests for anything correctness-critical; never commit secrets (see `.gitignore`); follow Conventional Commits (see `CONTRIBUTING.md`).

## Getting started

```bash
# nothing to run yet — this is a scaffold.
```

## License

[MIT](LICENSE) — © 2026 Oussama EL HADJI
