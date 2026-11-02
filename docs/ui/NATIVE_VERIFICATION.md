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

## Galaxy A32 physical verification

On 2 November 2026 the owner connected the SM-A325F running Android 13 / API 33 and authorised GitHub publication. Debug and test APKs were updated in place without clearing or uninstalling either app. All test databases were isolated synthetic fixtures; existing app data was not cleared.

- Full physical suite: **56/56 tests passed** in airplane mode, 116.079 seconds.
- All three host-controlled abrupt process-death checks passed on the phone: acknowledged recording, committed Finish and pending warm-down. Each fresh-process verification passed, retaining exact saved work and proving orderly cleanup did not run before the kill.
- With the actual phone system font scale set to 2.0, all **4/4 UiFoundationTest cases passed**, 20.281 seconds. This covers the new Home/navigation/Settings/library and visibility/retry paths; it is not full-app accessibility qualification.
- Restored and verified original phone settings: font scale 1.1, airplane mode off, stay-awake value 0, dark mode unchanged. No personal screenshot or workout data is included in this evidence.
- Inspected physical [Home](native-screenshots/a32/home-native.png), [Settings](native-screenshots/a32/settings-native.png), [Home at system 200%](native-screenshots/a32-system-200/home-native.png) and [Settings at system 200%](native-screenshots/a32-system-200/settings-native.png). The fixture's explicit theme/font comparisons are also retained separately.

## Visual evidence

[Native capture notes](native-screenshots/README.md) and [Home](native-screenshots/ui-evidence/home-native.png), [dark Home](native-screenshots/ui-evidence/home-dark.png), [enlarged text](native-screenshots/ui-evidence/home-200-percent.png), [Settings](native-screenshots/ui-evidence/settings-native.png).

Inspected Home light/dark/enlarged text and Settings captures. All data is synthetic. The theme comparison uses an explicit Compose font scale of 2; it is not device-wide Android accessibility qualification. Screenshots are native Compose captures, not a rendered all-screen pixel comparison with the browser reference. Empty/resume/failure states, retained manual routine chooser and unavailable editing/trends remain subject to owner visual review.

## Limits and remaining work

- API 37 Compose UI tests cannot initialise: the existing Espresso version calls removed android.hardware.input.InputManager.getInstance. Four new UI tests failed at framework initialisation before app assertions. No dependency changes or test quarantines were made. The same suite passes on API 33.
- Browser security refused the local prototype file URL. Its source/styles and behaviour specification were inspected. No rendered browser reference comparison or all-20-screen screenshot pass is claimed.
- A32 testing of this increment, including actual system 200% font scaling, passed as recorded above. Owner visual review, TalkBack, landscape and full-app target-device qualification remain outstanding. New/empty/resume/failure states were not fully designed in the package.
- Timers/countdowns, recording layout/correction/extra-set changes, notes, groups/removal, lifecycle editors, comparisons/progression and M3 features remain separate increments. DECISION_CHANGES.md records unresolved semantics. Do not interpret this foundation as implementing them.
- The initial publication block was resolved by the owner's subsequent GitHub authorisation. Documentation draft [PR #35](https://github.com/QuantumFighter/gym-tracker-android/pull/35) is open. The native increment remains separately reviewable and is published after adding this phone evidence. No merge or issue closure is authorised.
- GitHub's four named CI checks are tracked on the draft PRs. Local and physical results above do not substitute for their final status; owner visual approval remains pending.

## Owner review

Pending. Keep the documentation baseline and native increment separately reviewable. Do not automatically merge or start another implementation story. Review captured states and DECISION_CHANGES.md before the next affected increment.
