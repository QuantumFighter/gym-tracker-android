# Implementation plan

This is a proposed refinement sequence, not new sprint assignments. Follow the repository's one-implementation-story WIP limit, protected main, short-lived branches, squash PR review and required checks. Do not recreate existing issues or reopen completed stories wholesale.

| Increment | Main reference | Existing planning touchpoints | Outcome |
|---|---|---|---|
| Documentation baseline | Whole package | Agile process | Merge UI reference through documentation-only PR. |
| UI gap audit | Current native app vs specification | B-04/B-05 baseline, B-09 | Map every screen/control to implemented, UI-only change, missing behaviour, or deferred work. |
| Home/navigation/style | Home, Settings, Progress, shared type | B-09 (#15) | Minimal Home, navigation, preferences and active/empty states. Progress opens saved, read-only Workout history. Settings opens the read-only Exercise Library with simple name search. |
| Recording corrections | Exercise + Details | B-04 (#10), B-05 (#11), B-09 | Chronological edits, extra-set +, notes, data-safe UI. |
| Timers | Picker, countdown, warm-down, rest | B-06 (#12), B-09 | Countdown UX and robust real timer integration. |
| Selection/group changes | Exercise selection | B-08 (#14), B-09 | Multi-select/link/unlink with safe session mutations. |
| Programs/library | Editors and picker | B-11, B-12 | Native forms, routine editing, Library filters, multi-add and custom exercise editing. Preserve prior snapshots. |
| Summary/history/progress | Summary, history, graphs, progression | B-05, B-10 (#16), B-13 (#19) | Expand the saved History presentation, define comparisons for trends and add explainable recommendations. |
| Suggested programs | Suggestion setup/draft | B-18, M3 | Refined offline generator after prerequisites. |
| Release validation | All | B-16/B-17; B-19/B-20 for later types | Target-device/accessibility/recovery and scope qualification. |

Issue numbers in parentheses were observed in earlier repository retrieval in this conversation, not verified as current statuses here. B-keys without issue numbers intentionally require resolution. The current GitHub Project is the source of status and sprint priority.

For each increment: inspect actual code and requirements, document conflicts, choose a bounded scope, implement on a feature branch, exercise error states and real persistence, capture native screenshots, compare to reference, and request owner visual review before merge. Preserve the established four named CI checks; retrieve exact current workflow requirements rather than assume test counts.

## Documentation PR description

See `PR_DESCRIPTION.md`. Do not claim that this package implements Android features or validates the actual device.
