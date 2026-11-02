# Native verification - first UI increment

Date: 2 November 2026 (Australia/Brisbane). Base: merged B-05, 7a0a421. Scope and screen mapping: [NATIVE_GAP_AUDIT.md](NATIVE_GAP_AUDIT.md). This is a partial B-09 increment, not completion of the 20-screen implementation or M2.

## Implemented

- Packaged Barlow regular/medium match the approved font bytes exactly. The OFL notice ships in the APK. Light palette uses the approved hex values; dark equivalents follow the reference source.
- Home uses a 62 dp circular Play/Resume with a 33 dp symbol, 34 sp heading, 26 dp horizontal padding, Settings and compact three-destination navigation. Active sessions display their snapshot routine name and resume through the existing workout model. Empty/loading/storage failure and manual routine choice remain real.
- Progress is a separate route with real Workout history access and an honest unavailable trends state. History Back returns to its caller. Unsupported saved-routine editing is visibly unavailable. No invented time estimate or graph is displayed.
- Settings uses the reference's Completed exercises selector (Show with strikethrough / Hide from active list). It persists automatically through Room with committed-state failure/retry. Hidden completed exercises have a reveal action. Read-only searchable Exercise Library uses existing catalogue data.
- Schema v5 adds only ui_preferences. All 12 v4 table schema objects are unchanged. Cursor, set-save/correction and finalisation implementations are unchanged. Existing migrations remain registered.
- Native vectors use the reference's exact Lucide paths, including Settings sliders, Programs Layers and combined chart. Font/icon notices ship in app assets. No runtime dependency changes, downloaded runtime fonts, WebView or JavaScript port.

## Local checks

Passed on the final source:

```powershell
.\gradlew.bat --offline :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug :app:dokkaGeneratePublicationHtml spotlessCheck
```

All 13 JVM tests passed; lint reports No Issues Found. Formatting and Dokka passed. Exported v4/v5 schema comparison confirms exact equality of every existing table object, with only ui_preferences added. Font bytes match the reference assets exactly. Native increment diff whitespace checks passed; the imported reference baseline retains upstream formatting.

API 37 emulator: all 34 Room/database tests passed, including every existing persistence/migration test and two new preference tests. The future-version fixture now uses schema 99 because v5 is supported. No assertions were removed. A real v4 fixture with catalogue, plans, cursor, active session and finished recorded sets upgrades with exact content equality. The API 37 launcher started successfully.

## API 33 UI and persistence verification

Installed the official Google APIs API 33 image into the existing SDK and created a dedicated Pixel 2 AVD under the project build-tools directory. No project dependency/toolchain versions changed. The final complete suite passed **56/56 Android tests** in airplane mode (106.12 seconds), with no skipped/quarantined tests. The original emulator airplane mode was restored.

All six new tests pass: Home Play/navigation/History Back/Library; single-session Resume and revealing hidden completed work; failed preference write retaining the committed choice and successful Retry; native light/dark/font-scale captures; complete v4 content migration; preference reopen/cursor/Finish independence.

Legacy UI tests now find icon buttons by accessibility descriptions, enter History through Progress, scroll only descendants of scrollable content, and wait for Navigation 3's entering screen before clicking. Visibility assertions remain. The launcher recreation test checks approved brand/navigation instead of the removed foundation tagline. Initial failures exposed these obsolete assumptions and navigation timing; they were understood and corrected before the full passing run.

All three existing host-controlled abrupt process-death checks passed in airplane mode: recording after acknowledged save, finished history after committed Finish, and warm-down before Finish. Each killed the live process with Room open, proved orderly cleanup did not run, then verified preserved data and real navigation in a fresh process. The original airplane mode was restored. These are boundary checks, not qualification of every mid-transaction instruction or physical power/storage failure.

## Visual evidence

[Native capture notes](native-screenshots/README.md) and [Home](native-screenshots/ui-evidence/home-native.png), [dark Home](native-screenshots/ui-evidence/home-dark.png), [enlarged text](native-screenshots/ui-evidence/home-200-percent.png), [Settings](native-screenshots/ui-evidence/settings-native.png).

Inspected Home light/dark/enlarged text and Settings captures. All data is synthetic. The theme comparison uses an explicit Compose font scale of 2; it is not device-wide Android accessibility qualification. Screenshots are native Compose captures, not a rendered all-screen pixel comparison with the browser reference. Empty/resume/failure states, retained manual routine chooser and unavailable editing/trends remain subject to owner visual review.

## Limits and remaining work

- API 37 Compose UI tests cannot initialise: the existing Espresso version calls removed android.hardware.input.InputManager.getInstance. Four new UI tests failed at framework initialisation before app assertions. No dependency changes or test quarantines were made. The same suite passes on API 33.
- Browser security refused the local prototype file URL. Its source/styles and behaviour specification were inspected. No rendered browser reference comparison or all-20-screen screenshot pass is claimed.
- No Galaxy A32 is connected. Owner visual review, TalkBack, device-wide 200% scaling, narrow-screen/landscape and full target-device qualification remain outstanding. New/empty/resume/failure states were not fully designed in the package.
- Timers/countdowns, recording layout/correction/extra-set changes, notes, groups/removal, lifecycle editors, comparisons/progression and M3 features remain separate increments. DECISION_CHANGES.md records unresolved semantics. Do not interpret this foundation as implementing them.
- Git push of docs/approved-ui-handover was rejected by automatic approval review because specific external publication was not authorised. No remote branch, PR, issue or review message was created. Documentation and native PR descriptions are prepared locally in C:\QUT\Projects\gym-tracker-build-tools.
- GitHub's four named CI checks have not run for these local commits. This report describes local checks and direct emulator instrumentation, not CI or A32 approval.

## Owner review

Pending. Keep the documentation baseline and native increment separately reviewable. Do not automatically merge or start another implementation story. Review captured states and DECISION_CHANGES.md before the next affected increment.
