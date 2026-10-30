# Proposed architecture

Read [Decisions](DECISIONS.md) before implementation. This is a design, not an existing codebase.

## Verified guidance versus project choices

Checked 30 October 2026 against [Android architecture recommendations](https://developer.android.com/topic/architecture/recommendations): Android recommends UI/data layers, repository access rather than direct UI-to-storage calls, unidirectional data flow, Coroutines/Flow, ViewModels and lifecycle-aware state collection. Its current page recommends single-activity Compose applications and Navigation 3. A separate domain/use-case layer is conditional on complexity/reuse; it is not mandatory for every operation.

Project proposal: one Gradle application module, packages by feature with shared `data`, `domain` and composition-root packages. Extract modules only when measured build time or ownership warrants it. Use a small set of pure Kotlin rules for progression, rest, estimates and generation. Ordinary reads can go straight from ViewModel to repository; avoid a use-case wrapper for every DAO method. Use constructor injection and a small manual application container initially; Hilt remains optional. Navigation 3 and compatible stable dependency versions must be verified in the initial toolchain spike. No arbitrary version numbers are prescribed here.

## Component boundaries

| Component | Owns | Must not own |
| --- | --- | --- |
| Compose screens / Material 3 | Rendering immutable UI state, accessibility, user callbacks | DAO calls, progression calculations, authoritative timer state |
| Feature ViewModels | StateFlow screen state, loading/errors, action orchestration | Activity/context references, stored history as in-memory truth |
| Pure rules | Progression eligibility/explanations, rest round decisions, duration estimates, generator constraints | Android APIs, database access, system-clock reads |
| Repositories | Aggregate reads and transactional writes, mapping snapshots, backup/import coordination | Composable state, navigation |
| Room DAOs | Constraints, indexed queries, transactions and migrations | UI/product recommendations |
| Clock / file adapters | Monotonic and wall-time observations, selected-document I/O | Product progression policy |

Conceptual flow: user action → ViewModel → repository plus pure rule evaluation → Room transaction → Flow → ViewModel state → Compose. Read-only StateFlow is collected with lifecycle awareness. Pass IDs through navigation; retrieve durable state from storage. Saved-state facilities retain screen selection/drafts, not the workout record. Saving/error state is explicit, so navigation cannot falsely imply a successful write.

Proposed repository boundaries: ProgramRepository (plans and routine ordering), ExerciseRepository (catalogue and substitutions), WorkoutRepository (sessions and set transactions), SettingsRepository, BackupRepository (cross-aggregate import/export). Progress screens derive read models from workouts. Keep all authoritative data including small settings in Room for atomic complete import; DataStore is an optional later alternative, not a necessary extra store.

## Session state and transactions

`WARM_UP → RECORDING ↔ WARM_DOWN → FINISHED`; active states may become `ABANDONED`. Returning from warm-down is allowed before finalisation. Exercise status is independently pending, partial, complete or skipped. Incomplete final exercises with saved sets remain partial; none are relabelled failures.

Start copies the routine and prescription graph into session-owned records in one transaction. Save Set uses a stable command UUID: validate input and active state, insert the set once, update exercise/round state, replace timer state if appropriate, and commit before acknowledgement. A retry of the same command returns its existing result. A new intentional set uses a new command ID. Serialise commands and enforce one active session in storage. Finalise and Save Set racing must result either in a saved set included in the finalisation or an explicit rejection, never an orphan.

Draft weight/repetition controls may use saved UI state; approved policy: unfinished drafts are best-effort only, clearly separate from the saved table. Editing/deleting a saved set during an active session requires transactional recalculation; it must not restart an already issued round timer. Finalised records are read-only for MVP. Recovery resumes the session from Room, not the last navigation route.

## Timer model

Persist timer kind, running/paused/expired state, accumulated elapsed time for count-up timers or remaining duration for countdowns, monotonic anchor, wall-clock anchor and boot identity evidence. Rest changes are durable commands; compact and full-screen views observe the same timer. A UI ticker only refreshes display.

[SystemClock](https://developer.android.com/reference/android/os/SystemClock) documents that `elapsedRealtime()` includes deep sleep and is monotonic since boot. Use its delta on a verified same boot; do not decrement a persisted integer each second. Process death and screen-off then need no continuously running service. On a changed/unknown boot, use wall-time delta only as an explicitly approximate fallback, clamp negative remaining time to zero, flag implausible changes and offer correction. Time zones change formatting, not elapsed duration. Confirm the boot-detection approach in B-06; ambiguous detection must take the uncertainty path.

Approved MVP scope includes screen-off rest-expiry alerts with sound/vibration, respecting notification permission and settings. Verify current Android guidance and supported-device behaviour in B-06 before choosing a delivery mechanism; do not assume a foreground service or exact-alarm permission. Exact delivery timing is not promised. Persist alert identity with timer state; reschedule on adjustment and cancel on skip, replacement, Warm Down or Finish. Reconcile recovery without stale/duplicate alerts. Disabled alerts must not block recording. Countdown expiry alone does not complete a set or exercise. Do not automatically start rest after the final planned working set/round when no working work remains; manual rest remains available.

## Rules and estimates

Approved progression baseline: recommend one configured load increment after one completed session with at least three prescribed working sets, all reaching their upper rep targets at the target load. Four prescribed sets require all four; fewer than three prescribed sets permit manual increases only. Extra unprescribed sets cannot satisfy the minimum. Do not silently apply any recommendation.

Progression receives immutable history, prescriptions, equipment increments and context; returns `increase`, `hold`, `review` or `insufficient evidence` plus reasons and source session IDs. Matching requires the same exercise identity, measurement/load convention, target range/set count and superset membership/order policy. A substitution has its own history; do not convert machine weights between machines or infer equivalence from muscle group alone. Exclude warm-up and partial/skipped exercise records. No silent increase or reduction. Approved starting-weight rule (Q3b): use the most recent completed comparable exercise performance, show its date/source and allow override. Without comparable history, leave unset and explain. A single comparable performance is sufficient; there is no automatic 90-day expiry. Advanced recency/consistency checks are deferred. Present any progression increase separately so it is never silently applied.

Estimate remaining time from unperformed prescribed work, one remaining portion of the active rest, future eligible rest boundaries, transition allowance and optional warm-down allowance. Exclude removed/skipped work, avoid counting both current and full planned rest, and never show negative time. Use the approved fallback of 45 s per repetition-based set and 15 s per transition until enough personal observations exist; duration prescriptions use their actual duration. Supersets count rest per round. Display “Approx.” and use rounding, not a completion deadline.

Approved generator scope for M3 (Q6): training days, available equipment, session length and training goal. A versioned, reviewed template/rule set selects from supported catalogue entries with constraint checks, writes the ordinary editable program graph and explains choices or infeasible constraints. No exercise or load appears merely because a numeric target needs filling. No medical/personal training claims; the user reviews and edits the result. Advanced personalisation and adaptive programming are later wish-list items; experience/profile inputs are not included. Specific template content is reviewed during B-18.

## Persistence, files and privacy

Room is the authoritative offline store. Explicit migrations and exported schemas are planned; [Room migration guidance](https://developer.android.com/training/data-storage/room/migrating-db-versions) supports migration testing with schema history. Never use destructive fallback for workout data.

Use Android's system document picker for user-selected backup/import/export locations; broad storage access is unnecessary. JSON is versioned and complete; CSV is a flat reporting format. Validate and stage JSON before a transaction replaces data. See [Data model](DATA_MODEL.md).

[Android Auto Backup](https://developer.android.com/identity/data/autobackup) can transfer app data independently of an app's own sync feature. Approved policy: exclude workout data from automatic cloud backup and device transfer using platform-appropriate rules; test supported OS behaviour rather than assuming one manifest flag covers all manufacturers. User-selected exports may target cloud providers. No telemetry by default.

## UI structure

Approved navigation: top-level Home, Programs and Progress destinations; Exercise Library and Settings via secondary navigation. Workout is a focused active-session destination reachable from Home and a persistent Resume action outside Home where appropriate. No six-tab requirement. Home's empty state offers creation/selection; active session takes precedence over a new start. Keep Details out of primary recording layout. Warm Down remains one action away from full-screen timer, Details and exercise selection, with pending-work confirmation.

Use Material components, explicit semantics and [accessible touch targets](https://developer.android.com/develop/ui/compose/accessibility/api-defaults). Accessibility tests still validate custom plus/minus controls and timer announcements; do not announce every ticking second.
