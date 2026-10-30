# Testing strategy

This is an acceptance plan. No application tests have been run. [Backlog](BACKLOG.md) maps every requirement to story and scenario IDs; implemented tests should retain these IDs in names or test descriptions, and PRs link evidence.

## Levels and boundaries

Primary physical test device confirmed by owner: Samsung Galaxy A32, Android 13, One UI 5.1. Use it for recording usability, screen-off rest alerts and the NFR-05 performance baseline. Approved minimum supported version is Android 13 (Q7); supplement the physical device with Android 13 and the latest supported Android version in the release emulator matrix.

Use fast JUnit tests for pure rules and validation, coroutine test scheduling/fake clocks for ViewModels and flows, instrumented Room tests for constraints/transactions/migrations, and Compose UI tests for workflows and accessibility semantics. Use repository fakes at the UI boundary, but also retain a small real-Room end-to-end suite. Avoid tests of trivial getters or screenshots as the sole proof of correctness.

[Android's database testing guidance](https://developer.android.com/training/data-storage/room/testing-db) recommends running database tests on an Android device. [Room migration documentation](https://developer.android.com/training/data-storage/room/migrating-db-versions) describes exported schemas and migration helpers; schema validation must be supplemented with assertions about preserved user data.

## Scenario catalogue

| ID | Level | Required evidence |
| --- | --- | --- |
| T-01 | Instrumented + UI | Airplane-mode create manual program/routine → warm-up → out-of-order recording → warm-down → finish → relaunch → exact saved results. Empty library/routine states handled. |
| T-02 | Unit + Room | Range/type validation, unset versus zero load, unit round-trip, minimum/maximum steps, per-hand/assistance semantics, repeated command UUID and concurrent Save Set produce one acknowledged record. Disk-full failure shows unsaved state. |
| T-03 | UI | Minimal Home/recording layouts; Details reveals metadata; all six areas reachable; plus/minus and direct correction; completed visibility persists after relaunch; Warm Down reachable from every workout subview with confirmation. |
| T-04 | Unit + Room | Same-boot elapsed timer after background, deep sleep and process death; rest adjustment/skip/expiry, count-up stop/restart, wall clock jump, zone change, reboot/unknown boot, imported timer uncertainty and no negative countdowns. Fake clocks avoid real waiting. |
| T-05 | Unit + UI | Superset A/B and A/B/C, arbitrary member order, repeated member, unequal counts, warm-up sets, skipped member, partial exit, edit/delete, retry after crash and return to completed group. One round causes at most one rest; ordinary exercises use set rest. |
| T-06 | Room + UI | Kill process before/during/after save and finalisation, restore from storage, no duplicate set/active session. Activity recreation alone is insufficient. Abandon retains data; warm-down backtracking preserves sets. |
| T-07 | Room | Edit/rename/archive/duplicate routine or catalogue item after recording. Session snapshot, measurements, names and grouping unchanged. Substitute after some sets without relabelling old sets. Shared routine edits and deep duplication behave as disclosed. |
| T-08 | Unit | Double progression at below/exact/above threshold: one completed session with all prescribed working sets at full reps qualifies only when at least three are prescribed. Test 3/3 and 4/4 qualifying sets, reject 3/4 and 2/2 for automatic recommendations, and allow manual increases for two-set prescriptions. Extra unprescribed sets cannot bypass the minimum. Test overrides/manual mode and load increment rounding. Warm-up excluded; partial/skipped neutral; completed miss does not qualify or automatically reduce load. Changed targets/equipment/superset context cannot inherit incompatible evidence. |
| T-09 | Unit + UI | Starting weight from the most recent completed comparable exercise performance with date/source and override. A single observation qualifies; old history is dated, not automatically rejected. Absent/substituted/incompatible history leaves unset. Warm-up/partial records cannot supply the suggestion. No invented initial weight or silent progression increase. Progress shows history and evidence. |
| T-10 | Unit | Estimates for no work, ordinary sets, partial sets, supersets, adjusted active rest, skip/substitution and timed exercise. Count active rest once; no negative values; changing assumptions updates Approx. display. |
| T-11 | Room/instrumented | Every supported released schema → current, upgrade across multiple releases and clean install equivalence. Seed all entity types; assert values/counts/FKs/snapshots. Interrupted upgrade, corrupt-open and unsupported-version paths preserve original data. |
| T-12 | Unit + Room + document-provider integration | Complete JSON round-trip semantic equality, including custom/built-in references/settings/active sessions. Reject truncation, malformed fields, future format, duplicate IDs, broken references, excessive input and multiple active sessions. Cancel/fail before commit retains original DB; imported timer flagged uncertain. |
| T-13 | Unit + integration | CSV quoting for comma/quote/newline, Unicode, formula-like names and null metrics; units and superset context preserved; unfinished exercises represented. Picker cancellation, revoked URI access, full storage and provider failure report accurately. |
| T-14 | UI + manual device | TalkBack labels/order, 48 dp targets, 200% font size, contrast, dark mode, smallest supported screen, rotation, back navigation. Timers do not flood announcements. |
| T-15 | Device measurement | Reference-device 10,000-set dataset, 30 cold-start/save measurements, p95 targets from NFR-05; UI remains responsive and offline. Verify no unexpected network access, telemetry, broad storage permission or sensitive logs. Check approved OS backup rules. |
| T-16 | Unit + integration | Seeded generation is deterministic and persists the ordinary editable program model. Unsatisfiable equipment/time constraints explained, substitutions compatible, no fabricated weight. Generated routine runs through the normal workout workflow. |
| T-17 | Unit + UI + Room | Duration/distance forms, units, positive-value validation, persistence, history, backup and CSV. No weight progression applied to these types until a policy exists. |

## Regression and CI expectations

Approved-default regression coverage: T-02 checks metric units and configured increments; T-03 checks navigation, struck-through/hidden persistence and read-only final records; T-04 checks initial 90-second rest, overrides and reboot uncertainty; T-06 checks single-session resume, abandoned saved sets and warm-down corrections; T-07 checks independent duplication and both shared-edit options; T-10 checks the 45-second/15-second fallback; T-12 checks exact-limit acceptance and above-limit rejection for 25 MiB/100,000 sets with no database mutation on rejection. T-14/T-15 use the approved phone-first target. These are required scenarios for implementation, not tests already passed.

T-12/T-13 privacy regression: assert the exact JSON/CSV export allowlist and absence of profile/account fields using synthetic sentinel values at the export boundary. Non-identifying preferences and workout data must still round-trip. Reject unsupported profile/account sections on import. No real personal data is needed for these tests. T-15 verifies the approved automatic-backup exclusions on supported devices.

T-04 also includes real-device/instrumented screen-off rest-alert checks: notification permission granted/denied, disabled channel, sound/vibration settings, background/process recovery, adjustment rescheduling, cancellation and stale/duplicate suppression. Measure delivery latency separately from timer display accuracy.

Proposed required PR checks: `build`, `lint`, `unit-tests`, `android-tests`. Build assembles a debug APK; lint includes Android lint and the agreed formatter/static checks; unit tests cover rules/ViewModels; Android tests run critical Room/migration and workout UI smoke paths on one agreed emulator API. Use pinned JDK/Gradle/dependency versions and committed wrapper/schema history. Do not create workflow files until implementation approval.

Run a broader oldest-supported/latest-supported API matrix nightly or manually before release; budget emulator minutes explicitly. Database changes run all supported migration fixtures on the PR, and import/progression changes run their full affected suite. Failures block merge; do not retry until green without understanding a flake. File a linked defect for a quarantined non-critical test; never quarantine persistence/recovery checks to ship.

Before M2/M3 release: all mapped acceptance scenarios for that milestone pass, a real-device gym-flow smoke test is recorded, backup restore on a fresh installation is demonstrated, migration from the previous shipped build succeeds, and known issues are reviewed. Keep short reports and redacted failure artifacts; do not upload real workout backups to CI.

Unit coverage percentages are diagnostic, not a substitute for boundary cases. Every fixed bug gets a focused regression when behaviour warrants it. Security/dependency scans may be added as available, with an explicit triage policy rather than an unowned failing gate.
