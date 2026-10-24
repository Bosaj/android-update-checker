# Developer setup

## Requirements
- JDK 17 (Temurin works)
- Android SDK; Gradle installs SDK platform 36 on the first build
- Git 2.40+; Android Studio or any editor

## Build and test
```bash
git clone https://github.com/Bosaj/android-update-checker.git
cd android-update-checker
./gradlew testDebugUnitTest   # unit tests
./gradlew lintDebug           # Android Lint (fails on errors)
./gradlew assembleDebug       # app/build/outputs/apk/debug/app-debug.apk
```
`local.properties` points to the SDK. On Windows, escape the drive colon: `sdk.dir=C\:/Users/<you>/AppData/Local/Android/Sdk`.

### Windows: "Unable to establish loopback connection"
Gradle's Unix-domain sockets fail on long temp paths. Point the temp folder to a short path before building:
```bash
set TMP=C:\gtmp
set TEMP=C:\gtmp
set JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:\gtmp -Djava.io.tmpdir=C:\gtmp
```

## Run on a device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
The debug build installs as `ma.bosaj.updateradar.debug`, next to the release app.

Handy checks:
- Language: `adb shell cmd locale set-app-locales ma.bosaj.updateradar.debug --locales ar`
- Dark theme: `adb shell cmd uimode night yes`
- Stale apps on a fresh emulator: move the clock forward with `adb shell settings put global auto_time 0` and `adb shell cmd alarm set-time <epoch ms>`, and set `auto_time 1` afterwards.
- Reminder job: it has an initial delay equal to the interval, so `adb shell cmd jobscheduler run -f <package> <job id>` does nothing until the clock is past that delay.

## Release signing
Create a keystore outside the repository and a git-ignored `keystore.properties` (see the README), then:
```bash
./gradlew assembleRelease bundleRelease
```
See [[Play Store release|3-Play-Store-Release]] for the publishing steps.

## Contributing
Read [CONTRIBUTING.md](https://github.com/Bosaj/android-update-checker/blob/main/CONTRIBUTING.md): Conventional Commits, Semantic Versioning, all 8 languages for every new string.
