# Update Radar · رادار التحديثات — Wiki

**Update Radar** is an offline Android app that shows which apps have gone stale and takes you to the store that can update them — Play Store and Galaxy Store. It has **no internet permission**.

Current version: **v1.2.0** · [Download](https://github.com/Bosaj/android-update-checker/releases/latest) · [README](https://github.com/Bosaj/android-update-checker#readme) · [Changelog](https://github.com/Bosaj/android-update-checker/blob/main/CHANGELOG.md)

## What it does
- A radar of every app with a launcher icon: the store that installed it and the days since its last update.
- **Update all in Play Store** — opens Play Store's Updates page, where Play's own "Update all" updates every app that has a newer version.
- **One-tap updates (opt-in)** — an accessibility helper presses the stores' own buttons: **Update everything** = Play Store's "Update all" + every Galaxy Store app's Update; **Check all** walks every stale app hands-free. Apps the store shows as current are marked *On latest version*.
- **Check them all** — without the helper, each stale app's store page in turn; every return opens the next one.
- Optional **last used** dates, Wi-Fi reminders, a recently-updated feed and an ignore list.
- 8 languages with an in-app picker, including Arabic right-to-left.

## What it cannot do
Android does not let one app install updates for another, and there is no public API that says whether another app has an update. Update Radar **finds and routes**; with one-tap updates on, its helper presses the store's Update button for you, and the store does the update. "Stale" means *no update for N days*, not *an update is waiting*.

## Pages
- [[Architecture|1-Architecture]] — modules, data flow and the pure-logic core
- [[Roadmap|2-Roadmap]] — versions, milestones and what comes next
- [[Play Store release|3-Play-Store-Release]] — signing, App Bundle, listing and policy checklist
- [[Translations|4-Translations]] — languages, plurals, RTL and how to add one
- [[Privacy and security|5-Privacy-and-Security]] — permissions, data and supply chain
- [[Developer setup|Developer-Setup]] — build, test and run on a device
