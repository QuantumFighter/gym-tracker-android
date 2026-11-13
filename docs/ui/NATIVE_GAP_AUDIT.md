# Native gap audit — 2 November 2026

Baseline: merged B-05, `origin/main` 7a0a421. Live GitHub issue #11 is closed and PR #31 is merged; its older delivery prose is stale. B-06 onward remain open. The owner’s current instruction authorises native UI implementation from this package, superseding the packaging prompt’s read-only first-task limitation. Work is bounded to one reviewable increment at a time; this is not M2/M3 release approval.

## Screen and behaviour mapping

| Reference | Existing native entry/component | Gap and bounded increment | Backlog / decisions |
| --- | --- | --- | --- |
| Home | HomeScreen, HomeViewModel | Replace foundation text/buttons with approved hierarchy, Play/Resume, navigation; retain storage/retry/manual choice. No duration estimate until its rule engine exists. | B-09 #15; estimates B-10 #16 |
| Warm-up picker | WorkoutRoute WARM_UP | Missing. Countdown presets/custom entry replace count-up only with durable timer integration. | B-06 #12; custom bounds need refinement |
| Warm-up timer | WorkoutRoute WARM_UP | B-05 counts up. Stop Warm-up already enters recording and opens exercise selection. B-06 adds countdown, pause/resume and expiry presentation. Work Out retains the transition to exercise selection. | B-06 #12 |
| Selection | WorkoutRoute, WorkoutViewModel | Free order and explicit Complete/Partial exist. Add persisted completed visibility and accessible reveal; later cards/progress/group actions. | B-09 #15, B-08 #14; removal denominator and membership rules unresolved |
| Exercise | WorkoutRoute, NumberControl | Real durable save and correction dialog exist. Follow-up: chronological inline correction, table/editor layout and explicit extra-set opt-in. In that increment, reach Warm Down through Back to exercises and keep the unfinished-work confirmation (FR-07 / T-03). | B-04 #10/B-05 #11 completed, focused B-09 follow-up |
| Details | WorkoutRoute dialog | Snapshot prescriptions exist; full route, three independently expanded sessions and note editing absent. | B-09/B-13 #19; note identity/history semantics and comparison metric unresolved |
| Warm-down | WorkoutRoute WARM_DOWN | Safe confirmation/backtracking/finalisation exists; count-up conflicts with ten-minute countdown. | B-06 #12; owner must decide paused versus auto-start |
| Summary | HistoryRoute selected session | Snapshot-based read-only sets/status exist. Dedicated completion layout/expansion/duration missing. | B-05 baseline, B-13 #19 |
| History | HistoryRoute | Own-session selection exists; monthly cards and expandable tables missing. Move entry under Progress in navigation increment. | B-09/B-13 |
| Progress | No route | Add honest unavailable trends state plus working history entry; no sample graph. Define faithful metric before chart implementation. | B-13 #19; graph/comparison scope refinement |
| Programs | ProgramsRoute | Create/list/detail/add routine exist. Add shared bottom navigation; later approved cards and lifecycle editors. | B-09 subset; B-11 #17 |
| Routine | ProgramsRoute creation form | Validation, ordering, prescriptions persist. Saved editing missing; do not route Edit routine to creation or pretend it saves. | B-11 #17 |
| Library | ProgramsRoute catalogue picker | Add a read-only catalogue destination from Settings with simple case-insensitive name search. Filters, multi-add and custom exercise editing are later work. | B-09 #15 for read-only access, B-12 #18 for later Library work |
| Custom exercise | No form | Deferred native create/edit with domain metadata and supported recording kinds only. | B-12 #18; duration/distance B-19 #25, M3 |
| Exercise settings | ProgramsRoute ExerciseEditor | Per-set reps/load/rest creation exists; separate settings, replacement/progression routes absent. | B-11/B-12/B-13 |
| Settings | No route | Add Room-backed completed visibility with durable acknowledgement and retry. Other controls await real behaviour; no fake alert/estimate switches. | B-09 #15; alerts B-06, estimates B-10 |
| Program editor | ProgramsRoute creation | Name creation works; lifecycle editing/order/active program workflow absent. | B-11 #17 |
| Switch program | Home manual routine chooser | Cursor selection persists; dedicated explicit program confirmation absent. | B-11 #17; snapshots/history must stay unchanged |
| Suggested program | No implementation | Release-gated; no functioning-looking mock generator. | B-18 #24, M3; session length missing from prototype |
| Auto progression | No implementation | Use the approved Auto progression name and require acceptance before changing load. Under [Q3a](../DECISIONS.md#approved-product-and-project-decisions), one completed session qualifies when at least three working sets are prescribed and every prescribed working set reaches its upper rep target at the target load. Warm-up and extra sets cannot qualify. | B-13 #19 |

## First native increment acceptance mapping

FR-01 / T-03: Home has routine heading and 62 dp circular Play with accessible Record Workout label, active sessions Resume, settings action and Home/Programs/Progress navigation. Manual choice, loading, empty and storage failure remain real. Progress opens saved, read-only Workout history. Settings opens the read-only Exercise Library with case-insensitive name search. Unsupported editing and trends are explicitly unavailable. Shared palette/type derives from the prototype; no fabricated estimate.

FR-08 / T-03: completed work defaults to labelled strike-through, Hide completed exercises saves through Room, applies without a Save button, survives reopen, and offers Show completed exercises for review. A failed preference write retains the committed value and provides Retry. Preference writes cannot reset the next-routine cursor or change sessions/sets.

NFR-03 / T-11: additive schema v4→v5 creates a separate preference table, preserving all existing tables/fields and migration paths. Real Room tests cover preserved content, preference reopen and cursor/finalisation independence.

NFR-04 / T-14 subset: packaged Barlow regular/medium, approved light/dark palette, 48 dp targets, labelled navigation/icons, scrollable content and font scaling. Full TalkBack/A32/200% acceptance remains owner review; screenshots are evidence, not accessibility certification.

## Decisions still open

[Changes and open decisions](DECISION_CHANGES.md) records unresolved decisions about removal, group changes, notes, the History comparison metric, warm-down auto-start and custom duration limits. Countdown and recorder correction/rest layouts are deferred to their matching increments. Preserve durable set acknowledgement, stable correction identity/chronology, idempotency, Finish transaction/cursor advancement, immutable history and M3 gates.

## Verification boundary

The local prototype was read as source. The browser rejected its file URL, so no rendered reference comparison is claimed. No physical Android target was connected at initial review. See NATIVE_VERIFICATION.md for actual build/test/screenshot results and owner review status; do not infer completion from this plan.
