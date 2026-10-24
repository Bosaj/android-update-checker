# Google Play release

## Build the bundle
```bash
./gradlew bundleRelease   # app/build/outputs/bundle/release/app-release.aab
```
Signing comes from the git-ignored `keystore.properties` or the `UR_*` environment variables (see the README). Enroll in **Play App Signing**: the project key becomes the *upload key* and Google holds the app signing key. Keep a backup of the keystore and its passwords; without them you cannot upload updates.

## Listing
Everything the Play Console asks for is in `fastlane/metadata/android/` (fastlane `supply` layout), for `en-US`, `fr-FR`, `ar`, `es-ES`, `de-DE`, `it-IT`, `pt-BR`, `tr-TR`:
- `title.txt` (≤ 30), `short_description.txt` (≤ 80), `full_description.txt` (≤ 4000), `changelogs/<versionCode>.txt` (≤ 500)
- `en-US/images/icon.png` 512 × 512, `featureGraphic.png` 1024 × 500, `phoneScreenshots/*.png` 1080 × 2160 (Play's 2:1 maximum)

## Policy checklist
- [x] Target API 36 (required for new apps and updates from 31 Aug 2026)
- [x] **Privacy policy URL:** `https://github.com/Bosaj/android-update-checker/blob/main/docs/PRIVACY.md`
- [x] **Data safety:** no data collected or shared. The app has no internet permission, so nothing leaves the device. Last-used dates and the app list are processed on the device only.
- [x] **Package visibility:** launcher `<queries>` filter, no `QUERY_ALL_PACKAGES` declaration needed
- [x] **Usage access (`PACKAGE_USAGE_STATS`):** optional, behind an in-app prominent disclosure before Android's settings screen opens; the app works fully without it
- [x] **Foreground services:** none (WorkManager's foreground service is removed), so no declaration
- [x] **Notifications:** runtime permission on Android 13+, optional
- [x] **Ads:** none · **Target audience:** adults (not designed for children) · **Content rating:** everyone
- [x] No misleading claims: the listing says the store does the update
- [ ] Create the app in the Play Console, complete the questionnaires above, upload the bundle to internal testing, then production

## Release steps
1. Raise `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Update `CHANGELOG.md` and `fastlane/metadata/android/*/changelogs/<versionCode>.txt`.
3. Merge to `main` with green CI, tag `vX.Y.Z`, publish the GitHub release with the APK and its `.sha256`.
4. Upload `app-release.aab` to the Play Console.
