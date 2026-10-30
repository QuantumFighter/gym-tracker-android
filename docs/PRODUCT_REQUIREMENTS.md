# Product requirements

Status: approved development baseline, 30 October 2026. Product behaviours, defaults, technical acceptance targets and candidate implementation priorities are accepted through final planning approval. GitHub setup does not authorise application implementation. Approval records are in [Decisions](DECISIONS.md). Story and test mappings live in [Backlog](BACKLOG.md) to avoid a second tracking matrix.

## Consistency and feasibility review

- Local/offline operation is feasible for recording, progression and deterministic generation. Generation needs curated rules and exercise metadata, not a server or AI service.
- Minimal screens and six areas are compatible: secondary navigation and Details can hold metadata. Account means Settings until accounts are separately requested.
- Supersets and arbitrary exercise order require explicit grouping and round/rest policy; list order alone cannot determine rest.
- Autosave covers acknowledged completed sets. Unsubmitted controls are approved as best-effort drafts; losing them must never lose saved sets.
- Monotonic time survives screen changes, app backgrounding and process recreation on the same boot. Reboot plus an altered wall clock cannot yield perfectly recoverable elapsed time offline. Surface uncertainty rather than invent precision.
- Historical integrity requires session-owned prescriptions and exercise descriptions, not live joins to mutable routines.
- Complete JSON restore and CSV export serve different purposes. CSV is analysis output and is not a backup format.
- All confirmed features are retained. Scope is staged; generation and non-repetition measurements follow the manual-workout MVP.

## Priority and release definitions

P0 = first vertical slice or foundation for data integrity. P1 = manual-workout MVP. P2 = remaining initial-release features. P3 = later, explicitly outside initial release. These describe delivery order, not whether a confirmed requirement may be discarded. M1 = vertical slice; M2 = MVP; M3 = full initial release.

## Functional requirements

| ID | Priority / gate | Requirement and observable outcome |
| --- | --- | --- |
| FR-01 | P1 / M2 | Provide access to all six conceptual areas. Home shows next prepared routine, estimated duration, Record Workout and Edit Routine only, plus essential navigation/recovery/error states. No dashboard statistics. |
| FR-02 | P0 / M1 | Create a program and reusable routine manually; prescribe exercise sets, repetition targets/ranges, optional starting weight and rest duration. Invalid prescriptions cannot save. |
| FR-03 | P0 / M1 | Start a session from a routine; start/stop warm-up timing; select remaining exercises in any order and revisit partially recorded exercises without losing sets. |
| FR-04 | P0 / M1 | Record weight/repetition sets using prominent plus/minus controls and a clear Save Set action. Show exercise name, recorded sets table, current set controls, compact timer and navigation. Put extra metadata in Details. |
| FR-05 | P1 / M2 | Start configurable rest countdowns automatically between sets, adjust/skip rest and open a full-screen timer. Apply the approved superset policy without duplicate countdowns. Include screen-off rest-expiry alerts, respecting notification permission and sound/vibration settings. |
| FR-06 | P1 / M2 | Define supersets, preserve group/round context and allow free exercise order, including unfinished members and unequal prescribed set counts. |
| FR-07 | P0 / M1 | Mark exercises complete and return to selection. Warm Down remains reachable throughout an active workout; confirm if exercises remain. Finish preserves every saved performance record and distinguishes unfinished/skipped work. |
| FR-08 | P1 / M2 | Persist a setting to hide completed exercises or show them struck through. Hidden completed work remains accessible for review. |
| FR-09 | P1 / M2 | Show estimated total duration before starting and approximate remaining duration during a session, recalculating after progress, substitutions, rest changes or skipped work. |
| FR-10 | P1 / M2 | Edit, duplicate and archive programs; reuse routines and substitute exercises. Explain whether an edit affects linked routines. Existing sessions remain unchanged. |
| FR-11 | P2 / M3 | Generate programs offline using explicit deterministic rules and user constraints. Explain choices and infeasible inputs. Generated and manual programs use the same editable structure. |
| FR-12 | P0 / M1, expanded M2 | Supply built-in exercises; allow custom exercises with muscle groups, equipment, movement patterns, type, measurement format and substitution relationships by M2. |
| FR-13 | P2 / M3 | Record duration and distance exercises with type-appropriate units/validation. Weight/repetition is implemented first; other types must not masquerade as weight/repetition entries. |
| FR-14 | P1 / M2 | Suggest the most recent completed comparable exercise performance, showing its date/source and allowing override. One observation suffices, with no automatic age cutoff; absent comparable history leaves weight unset. |
| FR-15 | P1 / M2 | Default to double progression: one completed session, at least three prescribed working sets, every prescribed working set at its upper rep target. Fewer than three prescribed working sets use manual increases only. Retain configurable targets/increments and manual mode. Explain suggested increases; never silently apply them. Unsuccessful sessions prompt review, not automatic reductions. |
| FR-16 | P1 / M2 | Exclude warm-up sets and skipped/incomplete exercises from progression evaluation. Compare compatible exercise/measurement and superset context; preserve override decisions. |
| FR-17 | P0 / M1, hardened M2 | Persist completed sets before confirming success; recover interrupted sessions and timers. Recovery must not duplicate sets or start a second active session. |
| FR-18 | P0 / M1 | Preserve recorded prescriptions, names, units, performance and superset context independently of program/library edits, archiving or duplication. |
| FR-19 | P1 / M2 | Export complete versioned JSON and import a validated complete backup. Failed/cancelled imports leave existing data intact. Include non-identifying app preferences and interrupted sessions; exclude all profile/account data. Restore uses validated replace-all with preview, offer to back up current data and explicit confirmation. |
| FR-20 | P1 / M2 | Export workout history to CSV with stable identifiers, units, timestamps, set types, status and superset context. Correctly quote delimiters/newlines and neutralise spreadsheet formula text. |
| FR-21 | P1 / M2 | Progress shows past sessions, recorded sets and progression explanations. Advanced charts and predictive analytics are deferred. |

## Non-functional requirements

Approved device support (Q7): Android 13 and newer. Primary physical acceptance device is the owner's Samsung Galaxy A32 running Android 13 / One UI 5.1.

Accessibility/performance/timer measurement targets are recommended acceptance criteria for final review, with performance baselined in Sprint 1. Import limits are approved defaults.

| ID | Priority / gate | Acceptance target |
| --- | --- | --- |
| NFR-01 | P0 / M1 | All delivered core functions work in airplane mode, with one local user and no login, backend or mandatory network permission. |
| NFR-02 | P0 / M1 | A set acknowledged as saved survives process termination. Set insert, rest transition and session update are atomic and retry-safe; a failed save shows an actionable error. |
| NFR-03 | P1 / M2 | Every released database upgrade path preserves records and relationships. No destructive migration fallback in production. Recovery never silently resets corrupt data. |
| NFR-04 | P1 / M2 | Targets at least 48 dp, labelled controls and TalkBack reading order; recording usable at 200% font size on the agreed smallest supported phone. Do not communicate completion through colour/strike-through alone. |
| NFR-05 | P1 / M2 | On the agreed reference device with 10,000 sets: p95 save acknowledgement under 500 ms and cold Home usable under 2 s, across 30 measured runs. Keep disk work off the main thread. Baseline before finalising thresholds. |
| NFR-06 | P1 / M2 | UI, pure rules and persistence are separable; core rule tests require no Android runtime. Required CI checks pass before merge. |
| NFR-07 | P1 / M2 | Store data in app-private storage. No analytics or automatic network data transfer by default. Exclude automatic OS cloud backup/device transfer where supported. Export only on user request; exclude profile/account fields and warn that unencrypted workout files remain private data. |
| NFR-08 | P1 / M2 | After same-boot interruption, timer display is within 1 s of calculated elapsed time on resume. Reboot/clock uncertainty is visible and correctable. Screen-off rest alerts are included; exact background delivery timing is not guaranteed. Disabled notifications must not block recording. |
| NFR-09 | P1 / M2 | Reject malformed, incompatible or excessive imports safely; enforce the approved initial limits of 25 MiB input and 100,000 sets. Parse before replacing, validate references, and never log personal workout payloads. |

## Approved behaviour and defaults

Approved privacy clarification (Q5b, 30 October 2026), applying to FR-19, FR-20 and NFR-07: JSON/CSV exports must exclude all user profile/account data. “Complete” covers supported workout/program/library data and non-identifying app preferences. No user profile/account functionality is included in the initial release. User-initiated plaintext exports are approved with a privacy notice; exclude automatic OS backup/transfer where supported. Profile/account export requires future encryption and separate approval, and does not imply public availability.

- One active session. Start resumes it; explicit abandon retains saved sets with an abandoned status. Finalised history is read-only initially.
- Working versus warm-up is a per-set type. Whole-session warm-up timing does not turn recorded sets into warm-up sets automatically.
- Approved next-routine behaviour (30 October 2026): select an active program and cycle through its ordered routines after a finished session, wrapping to the beginning; abandoned sessions do not advance. Allow manual choice. Calendar scheduling is deferred.
- A confirmed Warm Down pauses new set recording, cancels rest and marks unfinished exercises incomplete/skipped according to whether sets exist; then a separate Finish action finalises the session. Saved-set corrections remain possible before Finish, including during warm-down. Warm-down duration is optional. Returning to recording before Finish restores pending work, retaining all sets.
- Superset rest: no automatic rest between members; one rest after all currently eligible members have logged a working set for the round. Skip/exclude a member explicitly, and do not trigger rest until a subsequent Save Set or explicit End Round. Already satisfied rounds cannot retrigger. Approved under Q2; members with no prescribed sets left leave later rounds, and no rest starts after the final round if no working work remains.
- Approved progression threshold (30 October 2026): recommend an increase after one completed session with at least three prescribed working sets, all of which reach their upper rep bound at the target load. Recommend one configured equipment increment, with user override. Incomplete sessions/exercises are neutral, not failures; below-target performance does not automatically reduce weight. All prescribed sets must qualify: four prescribed sets require all four. Exercises with fewer than three prescribed working sets have manual weight changes only; extra unprescribed sets do not bypass this rule. Approved starting-weight rule: use the most recent completed comparable exercise performance, display its date/source and allow override; absent comparable history leaves weight unset. No two-observation requirement or automatic age cutoff applies. These are product rules, not Android guidance.

Out of initial scope: accounts, cloud sync, wearables, social features, backend AI generation, advanced analytics, medical advice and automatic load reductions. No confirmed requirement is assigned to this exclusion list.

Approved defaults also include metric units with configurable weight steps; 90-second rest editable per exercise/superset; struck-through completed exercises by default; independent program duplication; best-effort unsaved drafts; 45-second set and 15-second transition estimate fallbacks; and phone-first navigation with secondary Library/Settings. See the complete approved-default table in [Decisions](DECISIONS.md). Later enhancements are listed once in the [backlog wish list](BACKLOG.md#deferred-wish-list).
