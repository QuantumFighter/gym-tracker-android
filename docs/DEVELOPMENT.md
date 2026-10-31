# Android foundation (B-01)

## Completion and history (B-05)

The owner authorised Issue #11 only on 31 October 2026, following merged B-04.
This section supersedes earlier increment boundaries below. B-06 is not implemented.

Complete Exercise explicitly completes an exercise with saved sets and returns to
selection. Complete is labelled and struck through; revisiting permits corrections
or additional recording. An additional new set reopens that exercise as Partial.
Saved-set corrections preserve explicit completion, set identity, units, original
recording timestamp/order and timer state. Optimistic comparison rejects stale edits;
failed writes retain previous saved values and the visible correction draft.

Warm Down is available from warm-up, selection, recording, Details and the correction
dialog. Pending work requires confirmation, rechecked in the repository transaction.
Confirmed warm-down cancels rest, pauses new sets and marks unfinished exercises
Partial if they have sets, otherwise Skipped. Saved-set corrections remain available.
Return to Recording restores Skipped to Not started, retains Partial/Complete and
every saved set. The optional count-up timer accumulates across warm-down visits.
Like B-04 timing, it assumes the same boot; reboot/alerts remain B-06.

Finish Workout is separate and requires warm-down. A single Room transaction marks
the session FINISHED with UTC completion time and offset, stops warm-down timing,
advances the source program's ordered routine membership (with wrap), and clears the
active reference. Repeating Finish for that session is a read-only success, including
after another workout starts. Save/correction commands serialize with finalisation;
late new writes are rejected. A known committed Save UUID may return its original
receipt after Finish without writing. Any failed transaction leaves the active
session, cursor and history unchanged. Abandon preserves sets/statuses and clears
the active reference in one transaction without advancing the cursor.

Home → Workout history lists finished sessions and opens read-only session-owned
names, prescriptions, load meaning, units, dates, sets and exercise statuses. No
current routine/catalogue values are used to render history. Abandoned sessions
remain in storage; the minimal history list includes finished workouts only.

Schema v4 adds session completion/warm-down fields, source membership lineage and
exercise status. MIGRATION_3_4 uses ALTER TABLE, derives existing Partial status from
saved sets, and preserves every v3 field/row. v1/v2 migrations remain registered.
Existing v3 sessions lack membership lineage: finalisation resolves the matching
routine within its source program, preferring the currently selected matching
membership. B-03 creates distinct routines, so this covers existing supported data.
New sessions snapshot the exact membership, including manual routine choices.

`WorkoutFinishDatabaseTest` exercises transactional failure, concurrency, corrections,
cursor wrapping and v3 migration. `WorkoutFinishUiTest` demonstrates the M1 workflow
and early ending through Compose with real Room. Its `finishPhase=seed/verify`
arguments support separate process verification. Run
`python scripts/verify_finish_abrupt_kill.py` with `ADB` set when needed: it performs
the manual M1 workflow, waits for Finish acknowledgement with Room open, force-stops
that PID without cleanup, and reads the original history through Home in a fresh
process. Set `FINISH_TEST` to
`com.quantumfighter.gymtracker.WorkoutFinishUiTest#t06WarmdownBeforeFinishSurvivesProcessDeath`,
`FINISH_PHASE_KEY=warmdownPhase` and `FINISH_MARKER=b05-pending` to run the corresponding
pre-Finish warm-down kill/recovery test. It resumes the same session, corrects a set,
restores pending work and then finishes. Both isolated synthetic fixtures run in CI.
These checks cover interruption before Finish and after its acknowledgement, not
every possible mid-commit kill timing.
Executed checks and device evidence are in [B-05 verification](verification/B-05.md).

B-04 is authorised by the owner's 31 October 2026 instruction for Issue #10.
B-01 through B-03 are merged. The dated boundaries below describe those earlier
increments; B-05 and later implementation remain outside this change.

## Workout recording (B-04)

Home's Record Workout starts the selected saved routine. Start during an active
session resumes it. Home and Programs offer Resume workout. Start Warm-up persists
a monotonic anchor; Stop Warm-up persists elapsed milliseconds and opens exercise
selection. Exercises can be selected and revisited in any order. Recorded work is
labelled Partial with its saved-set count, without an exercise-completion workflow.

The recorder separates Room-backed saved rows from current draft controls. Weight
accepts exact kg (three decimal places); blank and zero remain distinct. Prominent
labelled controls step weight by 2.5 kg and reps by one; direct editing remains
available. Working/warm-up set type is explicit. Details contains snapshot metadata
and prescriptions. Compact manual rest uses the prescribed duration and a persisted
monotonic anchor. Expiry only displays zero; alerts and advanced timer controls are
B-06. Timer anchors currently assume the same boot; reboot recovery is unqualified.

Room schema v3 adds five tables: workout_sessions, active_workout,
session_exercises, session_prescriptions and recorded_sets. MIGRATION_2_3 creates
only those tables/indexes; v1/v2 paths and the preserving open-helper remain active.
Session creation copies the full exercise definition, ordered prescriptions, names,
source identities and metric units in one transaction. There are no foreign keys
from snapshots back to editable templates. Save commands validate against the
session's immutable definition, serialize through Room transactions, retain UUIDs
for unchanged retries and return only after commit. Unique command/sequence indexes
provide a second guard. Same UUID with changed values is rejected. Global recording
order preserves interleaving across exercise occurrences for later superset work.

Drafts are best-effort ViewModel state, preserved across exercise switching but not
promised across process death. Failed saves retain their draft and command ID.
Saved sets recover from Room; no draft is promoted to a saved row by relaunch.
Partial status is derived from saved rows. Recorded sets are free entries associated
with a session exercise; no automatic prescription matching or round grouping is
introduced. Session prescriptions remain separately available for comparison.

Run WorkoutRelaunchTest with `-e workoutPhase seed`, force-stop the debug package,
launch MainActivity, then run the same test with `-e workoutPhase verify`. The test
compares the original complete session before seeding, retries the old command,
and resumes through the real app's Home/navigation against an isolated synthetic
database. CI runs this sequence as well as the full suite. See
[B-04 verification](verification/B-04.md) for executed results and limitations.

That original seed phase closes Room before force-stop. To test abrupt termination,
run `python scripts/verify_workout_abrupt_kill.py` after installing both debug APKs
(set `ADB` to the executable path if it is not on PATH). The host waits for an
acknowledged UI save with Room open, kills the live process without cleanup, and
verifies its isolated snapshot through the app composition in a fresh process.
CI runs both tests. This does not exercise production-default-database selection,
mid-transaction kill timing, reboot handling or automatic pending-draft recovery.

B-03 implementation is authorised by the owner's 31 October 2026 instruction for
Issue #9 only. B-01/B-02 are merged; the planning handover's earlier setup-only
statements are historical. B-04 and later stories remain outside this change.

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

## Manual programs and routines (B-03)

Home → Programs → Create program → Add routine opens the manual preparation form.
Name the routine, choose supported catalogue exercises, move occurrences up/down,
and add/remove working sets. Each set has a positive repetition range, optional
starting kg and nonnegative integer rest seconds (zero means no prescribed rest).
The initial editable draft is one 8–12-repetition set with 90 seconds rest and no
starting weight. Each additional set copies the preceding target for convenience.
Bodyweight entries take no external load; total/per-hand/assistance conventions
are explained beside the controls. Blank external weight is unknown, not zero.
Kilograms convert exactly to integer grams, accepting at most three decimals;
invalid text, fractional seconds, overflow and negative values cannot save.

Save routine commits its definition, ordered program membership, exercise
occurrences and ordered prescriptions in one Room transaction. Failure retains the
draft and shows an error. The saved detail can be reopened through Programs. Home
selects a saved membership as the next workout and retains that choice in Room.
The first routine is selected automatically. No session starts or cursor cycling
occurs yet; those behaviours need later stories. Drafts survive activity recreation
through the ViewModel but are best-effort across process death; saved routines and
Home's selection are durable. Back from a creation form discards that unsaved draft.

The single application-owned database is now schema v2. Explicit `MIGRATION_1_2`
adds `programs`, `routines`, `program_routines`, `routine_exercises`,
`set_prescriptions` and the minimal `app_settings` cursor. It never rewrites the v1
`exercises` table. Exported v1/v2 schemas are committed. Foreign keys use RESTRICT,
and parent/ordinal pairs have unique indexes. Repetition/load/rest combinations
and catalogue eligibility are validated at the domain/repository boundary, as with
B-02 metadata; SQL does not independently enforce all value-range invariants.
Future import/custom writers must use these same boundaries. Routine definitions
remain independent of program membership, allowing later reuse without schema
ownership changes. Session snapshots, shared-edit UX, advanced lifecycle workflows,
warm-up prescriptions, estimates and progression are not implemented in B-03.

`ProgramValidationTest` covers input/range/measurement rules on the JVM.
`ProgramDatabaseTest` covers full graphs, null/zero/exact mass, order/identity,
relationships, transactional failures, concurrent membership ordering, stale
selection, preserved v1 content and interrupted migration rollback.
`ProgramUiTest` exercises real Compose/ViewModel/Room creation and invalid drafts
against isolated synthetic files. `ProgramRelaunchTest` supports external `seed` and
`verify` phases via `-e programPhase`, comparing all catalogue rows, plan rows
(including prescription IDs) and cursor before any reseeding, then rendering Home
against that same isolated file. CI runs both phases around force-stop/launcher
restart, alongside the existing B-02 relaunch phases. See [B-03 verification](verification/B-03.md).

Programs tracks loading/observer failure separately from validation/save errors.
Retry remains available after a previously successful load and restarts one
terminated Room observation without clearing the draft or cached catalogue. Calls
while observation is active do not create duplicate collectors.
`ProgramsRecoveryTest` covers initial/post-load failure, visible Retry, draft/data
retention and live updates after recovery using a controlled failing stream around
the real Room repository.
