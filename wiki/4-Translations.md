# Translations, RTL and LTR

| Language | Folder | Direction |
|---|---|---|
| English (default) | `values` | LTR |
| Français | `values-fr` | LTR |
| العربية | `values-ar` | **RTL** |
| Español | `values-es` | LTR |
| Deutsch | `values-de` | LTR |
| Italiano | `values-it` | LTR |
| Português (Brasil) | `values-pt-rBR` | LTR |
| Türkçe | `values-tr` | LTR |

People pick a language in **Settings › Language** (or Android's per-app language settings). AppCompat recreates the screen, so Arabic switches the whole layout to right-to-left at once; `android:supportsRtl="true"` and Compose's layout direction do the mirroring.

## Rules
- Every string exists in all 8 files; counts use `<plurals>` with each language's quantities (Arabic: `zero one two few many other`; French, Spanish, Italian, Portuguese: `one many other`; English, German, Turkish: `one other`).
- Numbers use the UI language's digits (`localCount()`), so Arabic shows ٠١٢٣.
- In Arabic, counts are written in brackets — `(١٢)` — because the Arabic zero (٠) looks like the `·` separator.
- Big Arabic-Indic numbers use letter spacing 0: Android ignores negative tracking for them, and Compose would measure the text too narrow.
- Brand names stay as they are: Play Store, Galaxy Store, AppGallery, F-Droid.

## Add a language
1. Copy `values/strings.xml` to `values-xx/strings.xml` and translate it.
2. Add the locale to `res/xml/locales_config.xml`, `LANGUAGES` in `ui/screens/SettingsScreen.kt` and `localeFilters` in `app/build.gradle.kts`.
3. Add `fastlane/metadata/android/<locale>/` with the listing texts.
4. Run `./gradlew lintDebug` (missing or wrong plurals fail lint) and check the screens on a device.
