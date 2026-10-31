# Android foundation (B-01)

Implementation authority: owner instruction on 31 October 2026, **Issue #7 only**.
The nine planning documents remain the approved product baseline; their setup-only
authorisation statements describe the 30 October planning handover. #8–#11 and all
other implementation stories require separate authorisation.

## Toolchain

Versions checked on 31 October 2026 against official release notes and published Maven
metadata. Exact direct dependency versions are in `gradle/libs.versions.toml`.

| Component | Pin / rationale |
| --- | --- |
| Android Gradle plugin | 9.3.1 stable; supports API 37; requires Gradle 9.5.0+, JDK 17+ |
| Gradle | 9.6.0 official wrapper, distribution SHA-256 committed |
| Kotlin / Compose compiler / serialization plugin | 2.4.21 stable; upgrade built-in KGP through the documented root buildscript classpath |
| Build JDK | Java 25.0.4; local Android Studio JBR, CI Temurin identifier `25.0.4+7.0.LTS`; Java/Kotlin bytecode target 17 |
| Android SDK | min 33 (Android 13); compile/target 37 (Android 17); Build Tools 36.0.0 |
| CI SDK manager | Command-line tools 20.0, download build 14742923; explicitly install platform-tools, not the removed legacy `tools` package |
| Compose / Material 3 | stable Compose BOM 2026.09.00, no individual overrides |
| Navigation 3 | 1.2.0 stable, one serializable Home key; no other feature destinations |
| Activity / Lifecycle / Coroutines | 1.13.0 / 2.11.0 / 1.11.0 |
| JUnit / AndroidX Test | JUnit 4.13.2; extension 1.3.0; runner 1.7.0 |
| Formatting | Spotless 8.10.4 with ktlint 1.7.1 |
| API documentation | Dokka 2.2.0 stable; HTML generated from KDoc and module/package descriptions |

Minimum Android support is independent of compile/target SDK. API 37 is the current
stable SDK already installed on the development machine; the app uses no API above
33 without a guard. There is no Play publication requirement in this direct-APK project.
The installed Android Studio build is AI-262.10968.63.2622.16730706.

AGP 9.5.0 is newer, but 9.3.1 is within Kotlin 2.4.21's published fully supported AGP
range. Gradle 9.6.0 is also within that Kotlin release's supported range (through 9.7.0).
This deliberately pins a stable compatible combination rather than each latest version.

Official sources: [AGP compatibility](https://developer.android.com/build/releases/agp-9-3-0-release-notes),
[Kotlin compatibility matrix](https://kotlinlang.org/docs/gradle-configure-project.html),
[Kotlin releases](https://kotlinlang.org/docs/releases.html),
[built-in Kotlin upgrade](https://developer.android.com/build/releases/agp-9-0-0-release-notes#runtime-dependency-on-kotlin-gradle-plugin),
[Gradle Java compatibility](https://docs.gradle.org/9.6.0/userguide/compatibility.html),
[Compose BOM](https://developer.android.com/develop/ui/compose/bom),
[Navigation 3 releases](https://developer.android.com/jetpack/androidx/releases/navigation3),
[Android 17 SDK](https://developer.android.com/about/versions/17/setup-sdk),
[architecture guidance](https://developer.android.com/topic/architecture/recommendations).

## Open and build

Clone the repository and open its root in Android Studio; do not create another project.
Select the Java 25 Gradle JDK. Install SDK Platform 37.0, Build Tools 36.0.0 and Platform
Tools through SDK Manager. Accept the SDK licences. Set `ANDROID_HOME` or let Android
Studio create ignored `local.properties`. No machine-specific path belongs in Git.

PowerShell (use `./gradlew` on Linux/macOS):

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
.\gradlew.bat :app:lintDebug spotlessCheck :app:testDebugUnitTest
.\gradlew.bat --offline clean :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug spotlessCheck :app:testDebugUnitTest
```

The first run needs network access for the wrapper, plugins, dependencies and SDK.
Warm each requested task once before using `--offline`; merely downloading the wrapper
does not cache lint/test tooling. Offline builds need the same populated Gradle cache,
installed SDK and JDK. Runtime has no network permission, backend, login or telemetry.
Use `spotlessApply` to format; checks never apply edits automatically.

## Boundaries and injection

One `:app` module. `app/` is the composition/navigation root, `feature/home/` contains the
ViewModel, immutable screen state and rendering, and `ui/theme/` owns Material styling.
`HomeRoute` collects read-only StateFlow with lifecycle awareness. `HomeScreen` accepts
state directly and `HomeRoute` accepts an explicit ViewModel for tests; future real
repositories enter ViewModel constructors and are wired manually at the root.

B-02 adds Room 2.8.5 (runtime/compiler/plugin/testing) and KSP 2.3.13, retaining the
existing toolchain pins. `GymTrackerApplication` owns one lazy database/repository;
the composition root injects `ExerciseRepository` into Home's ViewModel. Home seeds
off the main thread, preserving the B-01 screen and showing loading/retry when needed.
`data/exercise/` owns schema v1, DAO, seeding and recording-selection guards; `domain/`
owns validated exercise values. See [Catalogue](EXERCISE_CATALOGUE.md) for all metadata,
licensing, load semantics and the schema contract. Programs, routines, recording,
history and progression remain later work.

The serialization **runtime family** is aligned with BOM 1.12.0 (stable, based on
Kotlin 2.4.10) while the compiler plugin remains Kotlin 2.4.21. Device schema testing
exposed AGP's app/test consistent resolution retaining app core 1.7.3 alongside
Room testing JSON 1.8.1, causing `GeneratedSerializer.typeParametersSerializers()`
`AbstractMethodError`. The BOM aligns both APKs without resolution forces or changing
the build toolchain. See the official [serialization release](https://github.com/Kotlin/kotlinx.serialization/releases/tag/v1.12.0).

The actual merged B-01 version catalogue pins AGP **9.5.0**, despite the older table/
report describing 9.3.1. B-02 preserves 9.5.0; no toolchain upgrade is made here.

Room's Gradle plugin exports compiler-generated schema JSON to `app/schemas/`; commit
it with every database change. Android tests validate schema v1 using the committed
asset and `MigrationTestHelper`, reopen a real file, check constraints and seed rollback.
After building, `git diff --exit-code -- app/schemas` must be clean. CI enforces this.
No migration is needed from B-01, which had no database. Never use destructive fallback.
The production open-helper factory also replaces SQLite's default corruption callback
and disables lossy open recovery; missing migrations and corruption are distinct paths.
Corruption fails into Home's error state without automatically resetting storage.
Android regressions cover actual corrupted-file open/retry and callback sidecar
preservation. Relaunch phases use an isolated database with a custom sentinel and
modified/archived built-in metadata, checking raw persisted rows before any seeding;
a negative test proves that database loss with retained preferences fails verification.

## API documentation

Document meaningful API responsibilities, state ownership, parameters and non-obvious
contracts using KDoc (`/** ... */`). Keep setup and architecture explanations in these
Markdown documents rather than repeating them on every declaration. Update KDoc in the
same change as the behaviour it describes. Dokka's module/package overview is
[`api/overview.md`](api/overview.md).

Generate the HTML reference from the repository root:

```powershell
.\gradlew.bat :app:dokkaGeneratePublicationHtml
```

Open `app/build/dokka/html/index.html` in a browser. Generated HTML stays in the ignored
build directory; source KDoc, the overview and pinned generator configuration belong in
Git. After the first dependency download, the same task works with `--offline`. Dokka
does not fetch external API indexes, so dependency types may appear without external
documentation links. Undocumented public declarations and generation warnings fail
the documentation task. Android framework members inherited unchanged are omitted.

In Android Studio, use **Quick Documentation** on a declaration to read its KDoc.
The HTML is generated locally; documentation publication and hosting are separate work.

Official references: [KDoc](https://kotlinlang.org/docs/kotlin-doc.html),
[Dokka Gradle tasks](https://kotlinlang.org/docs/dokka-gradle.html), and
[module/package documentation](https://kotlinlang.org/docs/dokka-module-and-package-docs.html).

## Tests and CI

Required check names: `build`, `lint`, `unit-tests`, `android-tests`. The workflow runs
actual Gradle tasks on pull requests and main, with read-only permissions and pinned
Action SHAs. Build also rebuilds clean outputs offline after warming dependencies.
Lint includes Android lint (warnings fail), formatting and Dokka HTML generation.
Android tests run the Room seeding/constraints/reopen/schema/failure suite, verify Home
retry state, launch the real activity, check rendered text/recreation and inspect
installed permissions on an API 33 Google APIs emulator in airplane mode. A separate
seed/force-stop/launcher/verify sequence compares the entire catalogue across processes.
Build checks that compiler output matches the committed schema. No APKs or signing material are
published or uploaded. CI caches build tooling only.

Only lint's `AndroidGradlePluginVersion` update advisory is disabled: it otherwise fails
deliberately compatible version pins simply because newer Gradle/AGP releases exist.
No correctness, accessibility or security lint findings are suppressed or baselined.

The manual workflow offers API 33 or 37 for the broader pre-release matrix. Both API
levels must pass before release, as well as the primary physical device. The complete
T-01 workout and T-15 privacy/performance acceptance scenarios remain later-story work;
these initial checks demonstrate only the foundation subset. Branch protection must
require the four names after their first successful real run, before merging.

## Galaxy A32 smoke test and signing

Primary physical device: Galaxy A32, Android 13 / One UI 5.1. Connect USB, enable USB
debugging, authorise this computer and verify `adb devices -l`. With only that target
connected (or `ANDROID_SERIAL` selecting it):

```powershell
.\gradlew.bat :app:installDebug :app:connectedDebugAndroidTest
adb shell am start -W -n com.quantumfighter.gymtracker.debug/com.quantumfighter.gymtracker.MainActivity
```

Check the screen in airplane mode, relaunch, rotation, light/dark mode and 200% font
size. Instrumented recreation does not prove process-death persistence; none is claimed.
Do not uninstall an existing data-bearing app or change device settings without restoring
them. This foundation writes no workout data. Backup exclusion rules cover cloud and
device transfer, but manufacturer behaviour needs later real-data qualification.

Stable release application ID: `com.quantumfighter.gymtracker`; debug uses `.debug` so
development does not replace a personal-use installation. Debug APKs use the local SDK
debug key and are for testing. Release is unsigned: no release key is generated here.
Before the first personal-use release, the owner must hold a private backed-up release
key outside the repository and configure signing locally; retain it for every manual
APK update. Losing/changing it prevents in-place upgrades. Public binary publication
remains outside this authorisation.

## NFR-05 performance baseline method

Use the A32 on Android 13 / One UI 5.1, record build SHA, OS/build, battery/thermal state,
animation settings and build type. Disable network, keep settings consistent and use
synthetic fixtures only. For the foundation, record 30 `am start -W -S` cold launches
and report median/p95 (nearest-rank sample 29 of 30) of activity `TotalTime`. This is an
initial launch proxy, not proof that future Home data is usable within 2 seconds.

Once B-04/B-05 supply recording and Home data, seed exactly 10,000 synthetic sets and
measure 30 cold Home usable times and 30 save acknowledgements. Measure save from user
action to committed-state UI acknowledgement with monotonic time, not just DAO duration.
Targets: p95 Home usable <2 s, save acknowledgement <500 ms. Use a profileable/release
build for qualification and retain raw redacted samples, device conditions and percentile
calculation. No 10,000-set/save measurement is possible in B-01; do not invent the model
or treat an empty debug launch as the final NFR-05 result.
