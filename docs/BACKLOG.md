# Initial product backlog

Ordered for implementation; B-keys are planning IDs, not GitHub issue numbers. All stories are **unstarted**. Approved product decisions are incorporated; story sequencing and technical implementation remain submitted for final planning review. Copy each story into an issue after approval, link its dependencies to actual issue URLs and use GitHub alone for live priority/status. Acceptance bullets are conjunctive. See [Testing strategy](TESTING_STRATEGY.md) for scenario details and [Release plan](RELEASE_PLAN.md) for sprint allocation.

## Epic issue bodies

| Epic | User outcome and completion rule | Children |
| --- | --- | --- |
| E-01 Platform and integrity | As the owner, I can trust a maintainable offline app; complete when its foundation, recovery and release quality stories pass. | B-01, B-06, B-07, B-16, B-17, B-20 |
| E-02 Manual plans and library | As a trainee, I can prepare and reuse appropriate routines; complete when manual authoring, catalogue and lifecycle criteria pass. | B-02, B-03, B-11, B-12 |
| E-03 Workout execution | As a trainee, I can record and finish a minimal flexible workout; complete when recording, timers, supersets and Home criteria pass. | B-04, B-05, B-08, B-09, B-10 |
| E-04 Progression | As a trainee, I can review results and make informed load decisions; complete when evidence-based recommendations and overrides pass. | B-13 |
| E-05 Data portability | As the owner, I can recover and analyse all my data; complete when JSON restore and CSV criteria pass. | B-14, B-15 |
| E-06 Full initial scope | As a trainee, I can generate editable plans and record other measurement types; complete when both extension stories pass. | B-18, B-19 |

## P0 — first usable slice

### B-01 — Establish the approved Android foundation

Epic E-01 · M1 · No dependencies beyond M0 approval · Requirements NFR-01, NFR-06 · Tests T-01, T-15.

As the developer, I want a reproducible offline build so small changes can be verified safely.

- Use approved Q7/Q8 choices (Android 13+, public QuantumFighter/gym-tracker-android, MIT, direct APK); confirm copyright-holder name and local implementation folder at setup. Verify stable compatible Kotlin/Gradle/JDK/Android/Navigation versions and record them.
- After explicit implementation authorisation, create agreed repository/Project, import issues, configure achievable branch protection and initial build/lint/unit/Android smoke checks.
- A clean checkout builds/runs a minimal Compose screen offline after dependencies are available; app runtime has no network dependency. UI/data/rule boundaries and test injection seams exist.
- Reference physical device: Samsung Galaxy A32, Android 13 / One UI 5.1. Approved minimum supported version is Android 13 (Q7). Record the performance baseline method and verify compile/target SDK choices separately. Do not build unrequested feature scaffolding.

### B-02 — Persist a small built-in exercise catalogue

Epic E-02 · M1 · Depends B-01 · Requirements FR-12, NFR-01, NFR-03 · Tests T-01, T-11.

As a trainee, I want known exercises available offline so I can build a routine.

- Seed a small reviewed/licensed repetition-based catalogue once with stable IDs, required metadata and load conventions.
- Relaunch preserves identities; seeding is idempotent and schema export begins at version 1.
- Unsupported measurement kinds cannot be selected for recording before B-19.

### B-03 — Create a manual program and reusable routine

Epic E-02 · M1 · Depends B-02 · Requirements FR-02 · Tests T-01, T-02.

As a trainee, I want to prepare sets and targets so I can start my own workout.

- Create/name a program and routine, choose catalogue exercises, order them and enter sets, rep range, optional load and rest.
- Invalid ranges/negative targets are rejected; an unknown initial load can remain unset.
- Reload preserves the routine; Home can select it for starting. Advanced lifecycle management belongs to B-11.

### B-04 — Start and autosave an out-of-order workout

Epic E-03 · M1 · Depends B-03 · Requirements FR-03, FR-04, FR-17, FR-18, NFR-01, NFR-02 · Tests T-01, T-02, T-06, T-07.

As a trainee, I want to save each set and switch exercises so my performance survives interruptions.

- Start snapshots the routine once; warm-up count-up starts/stops; remaining exercise buttons allow any order and revisits.
- Minimal recording screen includes labelled plus/minus controls, saved table, compact timer area/navigation and Details; saved versus draft state is explicit.
- Each acknowledged save is durable and command-ID retries cannot duplicate it. A failed save remains visible and retryable.
- Reopening a killed process restores acknowledged sets in the single active session; full timer/reboot hardening follows B-06.

### B-05 — Warm down, finish and read saved history

Epic E-03 · M1 · Depends B-04 · Requirements FR-07, FR-18, FR-21 · Tests T-01, T-03, T-06, T-07.

As a trainee, I want to finish a session without losing partial work so my history is accurate.

- Mark exercise complete and return to selection; Warm Down is reachable from all active workout subviews, confirming pending work.
- Finish preserves saved sets and partial/skipped distinctions; warm-down backtracking before Finish loses nothing.
- Finalised session appears in basic history after relaunch and remains unchanged after a routine edit; finalise/save races are deterministic.
- Approved MVP policy: allow saved-set corrections while active, including warm-down before Finish; completed workouts are read-only. Post-completion correction is a later backlog item.

## P1 — reliable manual MVP

### B-06 — Recover sessions, durable timers and screen-off rest alerts

Epic E-01 · M2 · Depends B-04, B-05; decision Q4 · Requirements FR-05, FR-17, NFR-02, NFR-08 · Tests T-04, T-06.

As a trainee, I want a correct resumed workout after interruptions so I can continue confidently.

- Same-boot screen-off/process-kill recovery meets the one-second target; paused/count-up/rest states recover correctly.
- Deliver screen-off rest-expiry alerts with sound/vibration subject to permission/settings. Validate the Android delivery mechanism, disabled-alert fallback, adjustment rescheduling and cancellation on skip/replacement/Warm Down/Finish. Prevent stale/duplicate alerts; document observed latency and platform limitations.
- Unknown/reboot clock state is visibly approximate and correctable; imported anchors cannot be mistaken for current-device time.
- One active session is enforced; resume/abandon choices retain committed performance. Verify boot evidence on supported devices.

### B-07 — Prove migrations and storage failure recovery

Epic E-01 · M2 · Depends B-04 · Requirements NFR-03, NFR-09 · Tests T-02, T-06, T-11.

As the owner, I want upgrades and failures to preserve data so an update cannot erase my history.

- Establish old-schema fixtures and a migration test path; all released schema paths become required checks as versions accrue.
- Tests assert content and constraints after upgrade, not only schema shape.
- Corrupt-open/interrupted-upgrade/disk-full paths preserve original data and provide actionable recovery without destructive reset.

### B-08 — Record supersets with configurable rest

Epic E-03 · M2 · Depends B-03, B-06; decision Q2 · Requirements FR-05, FR-06, FR-16 · Tests T-04, T-05.

As a trainee, I want free-order supersets and appropriate rest so I can alternate exercises naturally.

- Author groups and rest policy; sessions snapshot membership and assign sets to rounds.
- Ordinary sets start rest; grouped sets trigger one rest at the approved boundary, including explicit exclusions and unequal counts.
- Adjust/skip countdown and open full-screen timer without divergence; edits/retries cannot issue duplicate rest. Warm Down remains accessible.

### B-09 — Refine minimal navigation and persistent visibility

Epic E-03 · M2 · Depends B-05, B-08 · Requirements FR-01, FR-04, FR-08, NFR-04 · Tests T-03, T-14.

As a trainee, I want only necessary controls during training so recording remains quick.

- All six areas are reachable using the approved navigation; Home shows only prescribed content and essential empty/recovery/error states.
- A selected program yields the next routine with manual choice; Finish advances it, abandon does not; an active session resumes instead of starting another.
- Show struck-through or hide completed exercises persistently, with accessible status and a way to inspect hidden work; metadata lives in Details.

### B-10 — Estimate workout and remaining duration

Epic E-03 · M2 · Depends B-08, B-09 · Requirements FR-09 · Tests T-10.

As a trainee, I want an approximate duration so I can plan my available time.

- Home displays total estimate; active workout shows a clearly approximate remaining value.
- Changes to completed/skipped work, rest and substitutions recalculate without double-counting active rest or superset rounds.
- Empty/finished work never produces negative time; assumptions are inspectable in Details/settings.

### B-11 — Edit, duplicate, archive and reuse plans safely

Epic E-02 · M2 · Depends B-03, B-05, B-08 · Requirements FR-02, FR-10, FR-18 · Tests T-07.

As a trainee, I want reusable plans that can change without corrupting earlier workouts.

- Approved shared-routine editing: disclose affected programs and offer Update Shared Routine or Make a Separate Copy. The latter replaces only the current program's reference; completed and already-started workout snapshots remain unchanged.
- Program duplication deep-copies definitions; archiving prevents new selection without changing history.
- Editing targets/grouping/names leaves active snapshots and finalised records unchanged; unavailable next-routine selection is repaired visibly.

### B-12 — Add custom exercises and substitutions

Epic E-02 · M2 · Depends B-02, B-08, B-11 · Requirements FR-10, FR-12, FR-18 · Tests T-02, T-07.

As a trainee, I want equipment-appropriate alternatives so my routine works in my gym.

- Create/edit/archive custom entries with all specified metadata, measurement kind and load convention.
- Validate compatible substitutions; disclose changed targets and let the user confirm/reset them rather than blindly copying load.
- Replacing an exercise after some sets preserves original recorded identity/context and uses a separate occurrence for new work.

### B-13 — Review history and explain progression

Epic E-04 · M2 · Depends B-05, B-08, B-12; approved decisions Q3a/Q3b · Requirements FR-14, FR-15, FR-16, FR-21, NFR-06 · Tests T-08, T-09.

As a trainee, I want evidence-based load suggestions I can override so I remain in control.

- Pure-rule tests cover approved double-progression threshold: at least three prescribed working sets, every prescribed working set at its upper rep target in one completed session. Four sets require all four. Fewer than three prescribed sets permit manual increases only; extra unprescribed sets cannot bypass the minimum. Test increment and qualifying-history rules; UI explains reasons and source sessions.
- Suggest the most recent completed comparable exercise performance with its date/source and override controls. One comparable observation suffices, with no automatic age cutoff; absent/incompatible history leaves load unset. Warm-up/partial/skipped work cannot count as performance failure.
- Completed missed targets suggest review without automatic reduction. Manual mode and overrides persist; no suggested increase applies silently.
- History shows saved sets, grouping and relevant recommendation decisions. Split rule engine and UI sub-stories during refinement if needed.

### B-14 — Export and restore complete JSON backups

Epic E-05 · M2 · Depends B-07, B-11, B-12, B-13; approved decisions Q5a/Q5b · Requirements FR-19, NFR-07, NFR-09 · Tests T-12, T-13, T-15.

As the owner, I want a complete portable backup so I can recover all my data.

- System picker exports all authoritative model data, settings and active session from a consistent snapshot with format metadata.
- Import previews counts, offers a current backup and requires confirmation for replacement; malformed/unsupported/oversized input and cancelled operations do not alter existing data.
- Validate/stage before one transaction; fresh-install round-trip preserves semantic content and marks imported timers uncertain.
- Warn about plaintext export, avoid payload logs and implement approved OS backup/transfer rules. Use an explicit export allowlist excluding all profile/account fields; only non-identifying app preferences accompany workout/program/library data. Reject unsupported profile/account import sections.

### B-15 — Export useful and safe CSV history

Epic E-05 · M2 · Depends B-13, B-14 · Requirements FR-20 · Tests T-13.

As a trainee, I want readable history outside the app so I can analyse it myself.

- Export the documented columns with measurement units, snapshot labels, warm-up/working flags, statuses and group/round context.
- Include zero-set exercise status rows; quoting/Unicode/nulls/formula-like user text are tested.
- Cancellation, permissions and provider failures are truthful; explain that CSV cannot restore the application.
- Exclude all profile/account fields from CSV as well as JSON; verify the approved privacy boundary using synthetic sentinel data.

### B-16 — Verify accessibility, performance and offline privacy

Epic E-01 · M2 · Depends B-09, B-10, B-13, B-15 · Requirements NFR-01, NFR-04, NFR-05, NFR-07 · Tests T-03, T-14, T-15.

As a trainee, I want reliable, readable controls on my phone so I can use the app while training.

- Meet agreed font/target/semantics criteria, including full-screen timer and TalkBack navigation.
- Measure and report NFR-05 on the agreed device/dataset; correct blocking responsiveness problems.
- Complete airplane-mode flow; inspect permissions, logs and OS backup behaviour against the approved policy.

### B-17 — Qualify the manual MVP

Epic E-01 · M2 · Depends B-06–B-16 · Requirements NFR-02, NFR-03, NFR-06 · Tests T-01–T-15.

As the owner, I want release evidence so I can trust the app with real workouts.

- All M2 acceptance criteria pass, including actual process-death recovery, previous-schema upgrade and fresh-install restore.
- Required CI checks pass; known limitations and release/install instructions are documented.
- Real-device workout demonstration accepted, with no unresolved silent-loss defects. Distribution follows Q8; public binary publication requires separate authorisation.
- Approved MVP delivery is an APK for direct installation on the owner's Galaxy A32, with documented manual update steps and protected signing-key custody. Verify an upgrade preserves data. Google Play publication is deferred.

## P2 — complete the initial release

### B-18 — Generate editable programs offline

Epic E-06 · M3 · Depends B-11, B-12, B-13, B-17; decision Q6 · Requirements FR-11, FR-14 · Tests T-09, T-16.

As a trainee, I want a suggested starting program so I need not build every plan from scratch.

- Agreed inputs run through deterministic reviewed rules with equipment/time validation and explanations for infeasible requests.
- Approved inputs are training days, available equipment, session length and training goal. Use a small reviewed template set; defer advanced personalisation and adaptive programming to the wish list.
- Results use ordinary program/routine entities, preserve generator provenance and are fully editable/duplicable.
- No reliable history means no invented starting load; generated routines work with normal recording and backup.

### B-19 — Record duration and distance exercises

Epic E-06 · M3 · Depends B-12, B-14, B-15, B-17 · Requirements FR-13 · Tests T-02, T-10, T-12, T-13, T-17.

As a trainee, I want timed/distance exercises represented accurately so my routine is not limited to repetitions.

- Enable type-specific prescriptions and entry controls with explicit units; preserve them through recovery/history/export/import.
- Estimates use available duration information and label uncertain distance estimates; never silently convert distance to reps.
- Weight/repetition progression does not apply to these types. Existing repetition workouts remain unchanged and schema/backup upgrades pass.

### B-20 — Qualify the full initial release

Epic E-01 · M3 · Depends B-18, B-19 · Requirements FR-11, FR-13, NFR-01–NFR-09 · Tests T-01–T-17.

As the owner, I want the complete agreed scope verified so I can approve the initial release.

- Every FR/NFR has passing linked acceptance evidence or an explicitly approved scope amendment; no silent deferral.
- Generated/manual routines and all measurement types complete end-to-end, migration, restore and accessibility regression.
- Review risks and known issues, tag the approved release and distribute only to the agreed destination.

## Deferred wish list

These items are outside the initial-release commitment. Do not schedule them, implement them or create a second delivery tracker for them by default. Promote an item to an issue only when scope is explicitly accepted.

| Later enhancement | Boundary / prerequisite |
| --- | --- |
| Editing completed workouts | MVP permits corrections only before Finish; future editing needs progression/history correction rules |
| Merge restoration | MVP replaces all with approved safeguards; future merge needs duplicate/conflict handling |
| Password-protected backups | MVP uses user-initiated plaintext exports excluding profile/account fields |
| Account/profile functionality and export | Outside initial release; profile/account export requires encryption and separate approval |
| Advanced generation/personalisation | M3 uses the approved four inputs and reviewed offline templates; adaptive programming deferred |
| Advanced starting-weight reliability | MVP uses most recent comparable performance; recency/consistency scoring deferred |
| Calendar routine scheduling | MVP uses selected-program cycling and manual choice |
| Dedicated tablet/foldable layouts | Phone-first initial release |
| Google Play publication | MVP is directly installed APK with manual updates |
| Advanced analytics and external integrations | Cloud sync, wearables and social features require separate scope; no implicit backend work |

Automatic load reductions and medical advice are excluded behaviours, not promised future enhancements.

## Traceability maintenance

Owner preference: keep optional enhancements off the initial-release delivery path. Merge restoration and password-protected backups are later wish-list candidates, not committed stories or milestones. Q5b approves manual plaintext exports without profile/account data and excludes automatic OS backup/transfer where supported. Future profile/account export is gated on encryption and separate approval; confirmed initial-release functionality remains in scope unless explicitly changed.

All FR-01–FR-21 and NFR-01–NFR-09 occur in the story mappings above. T-01–T-17 define verification, not pre-existing passing tests. A new/changed requirement updates its owner story and affected scenario; a bug links the original requirement and adds a regression. Sprint tables refer to story keys and gates rather than duplicate acceptance criteria or status.
