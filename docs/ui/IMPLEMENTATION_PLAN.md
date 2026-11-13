# Implementation plan

This is a proposed refinement sequence, not new sprint assignments. Follow the repository's one-implementation-story WIP limit, protected main, short-lived branches, squash PR review and required checks. Do not recreate existing issues or reopen completed stories wholesale.

| Increment | Main reference | Existing planning touchpoints | Outcome |
|---|---|---|---|
| Documentation baseline | Whole package | Agile process | Merge UI reference through documentation-only PR. |
| UI gap audit | Current native app vs specification | B-04/B-05 baseline, B-09 | Map every screen/control to implemented, UI-only change, missing behaviour, or deferred work. |
| Home/navigation/style | Home, Settings, Progress, shared type | B-09 (#15) | Minimal Home, navigation, preferences and active/empty states. Progress opens saved, read-only Workout history. Settings opens the read-only Exercise Library with simple name search. |
| Recording corrections | Exercise | B-04 (#10), B-05 (#11), B-09 (#15) | Chronological edits, explicit extra-set + and Warm Down via selection. This is the reserved recorder increment; keep notes/history/Details outside it. |
| Details follow-up | Details | B-09 (#15), B-13 (#19) | Independently expandable previous-session summaries and notes only after comparison/note-identity decisions; do not expand the active recorder PR. |
| Timers | Picker, countdown, warm-down, rest | B-06 (#12), picker #33, countdown #34, B-09 (#15) | Countdown UX and robust real timer integration. |
| Selection/group changes | Exercise selection | B-08 (#14), B-09 | Multi-select/link/unlink with safe session mutations. |
| Programs/library | Editors and picker | B-11 (#17), B-12 (#18) | Native forms, routine editing, Library filters, multi-add and custom exercise editing. Preserve prior snapshots. |
| Summary/history/progress | Summary, history, graphs, progression | B-05, B-10 (#16), B-13 (#19) | Expand the saved History presentation, define comparisons for trends and add explainable recommendations. |
| Suggested programs | Suggestion setup/draft | B-18 (#24), M3 | Refined offline generator after prerequisites. |
| Release validation | All | B-16/B-17; B-19/B-20 for later types | Target-device/accessibility/recovery and scope qualification. |

## Reconciliation checkpoint: 13 November 2026

PR #35 merged the reference package and PR #36 merged the Home/navigation and durable-visibility foundation. These are not completion of all B-09 or M2. Existing Home issue #32 needs acceptance/dependency reconciliation rather than duplicate implementation: saved-routine editing belongs to #17 and estimates to #16. Do not infer owner visual approval from a merge alone.

PR #37 is the active, separately owned recorder increment. Do not alter its worktree or assign its inline correction, explicit extra-set or Warm Down navigation work again. At this checkpoint it is open, not a completed milestone. Recheck live status before acting; this dated note is not a second status tracker.

Keep #33 scoped to preset/custom selection and #34 to active countdown/expiry; both trace to #12 for durable timer/recovery qualification. Remove their earlier Get Ready wording, hide the custom trigger during entry, and use full-screen green Work Out at expiry. Preserve the broader alert/reboot requirements rather than declaring #12 complete when a screen is finished.

Suggested refinement order after current review: bounded timer slices, selection/group changes, programs/library lifecycle, then history/progression, with Details work split as dependencies allow. This is a dependency-aware candidate order, not a change to Project fields or a promise to bypass a product decision. The original recording increment is already reserved. Maintain the existing epics, issue relationships, M2/M3 gates and one-implementation-story WIP limit.

Blocking decisions remain in DECISION_CHANGES.md: custom duration bounds, warm-down auto-start, session removal/undo/progress denominator, mixed/mid-round grouping, note identity/history and comparison metrics. Ask only before the affected slice; do not infer production semantics from demo constants. Generator session-length reconciliation remains M3. The GitHub Project remains authoritative for live priority, status, iteration and estimates.

For each increment: inspect actual code and requirements, document conflicts, choose a bounded scope, implement on a feature branch, exercise error states and real persistence, capture native screenshots, compare to reference, and request owner visual review before merge. Preserve the established four named CI checks; retrieve exact current workflow requirements rather than assume test counts.

## Documentation PR description

See `PR_DESCRIPTION.md`. Do not claim that this package implements Android features or validates the actual device.
