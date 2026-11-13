# Changes and open decisions

Latest explicit owner feedback wins for the listed UI changes; reconcile planning documents through a reviewed PR rather than silently rewriting architecture.

| Previous expectation | Approved later decision |
|---|---|
| Home Record Workout rectangle | Circular Play beside routine name; larger circle and icon. |
| Bright white / green first draft | Soft blue-grey surfaces with bright blue actions. Green retained for Work Out / completion. |
| Count-up warm-up in B-04 | Preset/custom countdown, selection starts immediately. |
| Get Ready labels | Removed from duration and countdown screens. |
| Custom-time button stays visible | Hide it once its entry controls open. |
| B-05 Stop Warm-up enters recording and opens exercise selection | Equal-width pause/play and green Work Out, with full-screen Work Out at expiry. Work Out retains the transition to exercise selection. |
| Warm Down on every active subview | In the recorder follow-up, use Back to exercises to reach Warm Down from individual recording. Other approved access points and unfinished-work confirmation remain (FR-07 / T-03). |
| Expandable rest timer | Individual recording now uses a slightly larger centred inline timer, small black boxed pause/play; no expand action. |
| Persistent next-set form | Hide after prescribed count; explicit small + for extra sets. |
| Corrections outside sequence | Inline correction at the selected set's chronological position. |
| Workout selection generic headings | Routine name is the main heading; Warm Down at bottom. |
| Superset setup only in planning | Session multi-select via hold, link and unlink; dissolve groups below two members. |
| Warm-down elapsed timer | Ten-minute countdown, ±1 minute controls, no timer/summary labels. |
| Progress as session list | Progress contains graphs; Workout history is a separate destination. |
| One previous-session table | Three visible one-line date summaries; each expands independently. |
| Notes display only | Pencil in expanded Instructions & notes opens inline save/cancel. |
| Double progression label | Auto progression. This changes naming, not automatic application of weight. |

[Approved Q3a](../DECISIONS.md#approved-product-and-project-decisions) governs Auto progression. One completed session qualifies when at least three working sets are prescribed and every prescribed working set reaches its upper rep target at the target load. Warm-up and extra sets cannot qualify. Recommendations require acceptance before changing load.

## Resolve before the affected implementation

1. Define the History comparison metric for mixed load, reps and assistance, including comparisons outside the three visible dates.
2. Define current-workout removal: keep saved sets, classify partial/skipped, define denominator and undo/re-entry behaviour.
3. Define mixed multi-selection, editing existing superset membership mid-round, and preservation of prior set/round context.
4. Decide note identity (catalogue-level versus routine-specific overrides) and historical note presentation. Owner requested notes directly on the exercise, not destructive historical edits.
5. Timer defaults: warm-down opens at 10:00 paused in demo; warm-up selection starts immediately. Confirm whether production warm-down should auto-start. Do not infer full-screen warm-down expiry; none was requested.
6. New/empty/resume/save-failure states need visual review; they were mentioned but not fully designed. Demo Resume icon is illustrative.
7. Custom duration bounds, decimal precision, increment bounds and maximum sets follow domain constraints, not demo constants.
8. The generator mock omits session length despite the release plan requiring it; refine before B-18. Its experience/exclusion inputs are UI concepts, not approval for medical recommendations or adaptive generation.
9. Completed visibility, estimates, generator and recording-type UI must respect release gates. This design package does not move M3 work into M1/M2.

Repository documents checked on 2 Nov: `docs/AGILE_PROCESS.md` (blob cfe821b128d017d6f28c654c26675fd64a7b31bd) and `docs/RELEASE_PLAN.md` (blob 85b9ebccea738c34c7196e858d607b6f75a3d321). Both retain setup-era wording; do not use their “unstarted” prose as current issue status. Root AGENTS.md fetch returned 404, which is not proof that no nested instructions exist. Implementation must inspect its actual checkout and live issue/PR status.
