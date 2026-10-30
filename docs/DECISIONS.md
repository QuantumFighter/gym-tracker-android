# Decision log

30 October 2026. **Only explicitly recorded decisions are approved; implementation remains unauthorised.** Confirmed items originate in the brief; proposed items are recommendations. Record owner/date and rationale when accepting or changing a proposal.

## Confirmed constraints

| ID | Decision | Consequence |
| --- | --- | --- |
| C-01 | This stage is documentation only | No code, repository creation or issue publication until approval |
| C-02 | Kotlin/Compose/Material 3, Room, ViewModel/StateFlow/Coroutines, Gradle Kotlin DSL, JUnit and Android UI tests | Use the requested stack; verify compatible versions at setup |
| C-03 | Local-first, offline and single-user; no backend or authentication | Settings replaces any account functionality |
| C-04 | Manual complete-workout persistence is the first functional objective | Generation and analytics cannot displace the vertical slice |
| C-05 | Editable manual/generated plans, immutable historical performance | Session snapshots are necessary; edits must not rewrite history |
| C-06 | Progression is explainable and overridable; no automatic reductions | Preserve evidence and distinguish incomplete/skipped from failed targets |
| C-07 | Complete JSON backup/import and CSV export | Data portability is part of the manual MVP proposal, not deferred indefinitely |

## Architecture baseline accepted in final planning approval

| ID | Proposal and rationale | Alternative / revisit trigger |
| --- | --- | --- |
| ADR-01 | One app module with UI/data boundaries and selected pure domain rules; small enough for solo maintenance | Extract modules when build/ownership benefits justify cost |
| ADR-02 | Room stores workouts, settings and plans, enabling one atomic complete restore | DataStore for preferences if independent settings lifecycle becomes valuable |
| ADR-03 | Session-owned snapshots plus immutable final records; nullable source lineage | Full event sourcing adds unnecessary complexity; history correction would need an explicit later design |
| ADR-04 | Transactional, command-ID-based set saves and a persisted timer model | Background tick persistence wastes work and introduces drift; exact alerts require separate design |
| ADR-05 | Pure rules return recommendations/reasons; ViewModels orchestrate UI | Do not duplicate rules across screens or encode them in SQL |
| ADR-06 | Versioned JSON with staged validation and transactional replace; CSV for reporting only | Merge restoration and password-protected backups are deferred to the wish list; profile/account export requires encryption and separate approval |
| ADR-07 | Manual constructor injection and current stable Navigation 3, subject to compatibility spike | Hilt if construction complexity grows; reconsider navigation only with concrete compatibility evidence |

These are project tradeoffs, not claims that Android requires this precise design. Official recommendations and verification date are in [Architecture](ARCHITECTURE.md).

## Approved product and project decisions

| Decision | Approved choice | Approval record and implementation notes |
| --- | --- | --- |
| Q1: Release boundary — APPROVED | M1 small manual slice; M2 reliable manual MVP; M3 generation plus duration/distance | Approved by owner on 30 October 2026: “I approve the staged release scope.” All confirmed features remain in scope; generation and duration/distance follow the manual MVP. This does not authorise implementation. |
| Q2: Superset/rest semantics — APPROVED | Rest once after one working set from each participating member of a round; free exercise order; explicit member exclusion | Approved by owner on 30 October 2026. Warm-up sets do not complete working rounds; members with no prescribed sets left leave later rounds. No automatic rest after the final round if no working work remains. Manual rest start/adjust/skip remains available. |
| Q3a: Progression threshold — APPROVED | Recommend an increase after one completed session when at least three working sets are prescribed and every prescribed working set reaches its upper rep target at the target load. | Owner clarified on 30 October 2026: three or more sets must reach completion; two-set exercises permit manual increases only. Four prescribed sets require all four, not just three. Warm-up sets are excluded; manual changes and recommendation overrides remain available. |
| Q3b: Starting-weight suggestions — APPROVED | Use the most recent completed, comparable exercise performance; show its date/source and allow override. No comparable history means unset weight. | Approved by owner on 30 October 2026. Supersedes the two-observation/90-day proposal. Do not transfer loads between different exercises/equipment. Advanced recency/consistency checks are later wish-list items. |
| Q4: Screen-off rest alerts — APPROVED | Include screen-off rest-expiry alerts in M2, respecting notification permission and sound/vibration settings | Approved by owner on 30 October 2026. Exact background delivery timing is not promised. Validate delivery mechanism on supported Android versions in B-06. Approximate/correctable reboot recovery was approved with the defaults bundle. |
| Q5a: Restore behaviour — APPROVED | Replace-all JSON with validation before changes, preview of date/counts, offer to back up current data and explicit replacement confirmation; failed import leaves current data intact | Approved by owner on 30 October 2026. Merge restoration belongs on the later wish list, with no initial-release commitment. |
| Q5b: Backup privacy — APPROVED | User-initiated unencrypted JSON/CSV exports; exclude automatic OS backup/transfer where supported; exclude all user profile/account data from exports | Approved by owner on 30 October 2026 with profile-data exclusion. Exports include workout/program data and non-identifying app preferences only. Profile/account export cannot be introduced before encryption and separate approval. Encryption and accounts remain later scope; encryption alone does not authorise public disclosure. |
| Q6: Limited generation scope — APPROVED | Offline reviewed templates/rules using training days, equipment, session length and goal; ordinary editable programs, explanations and infeasible-constraint feedback | Approved by owner on 30 October 2026 for M3. Advanced personalisation and adaptive programming go on the later wish list. Experience/profile inputs are not part of this approved input set. Specific template content still requires review during B-18. |
| Q7: Minimum Android version — APPROVED | Android 13 and newer. Primary physical test device: Samsung Galaxy A32 running Android 13 / One UI 5.1. | Owner approved minimum support on 30 October 2026. Phone-first layout and deferral of dedicated tablet/foldable layouts were approved with the defaults bundle. Verify dependency compatibility and current compile/target SDK requirements in B-01; minimum support is distinct from compile/target SDK. |
| Q8a: Repository visibility — APPROVED | Public GitHub repository | Owner chose public on 30 October 2026 for GitHub feature availability and open project contents. Never commit personal workout data, backups, credentials or signing secrets. Changing visibility later cannot recall copies already obtained. Repository creation and GitHub setup were subsequently authorised on 30 October 2026; Android implementation remains unauthorised. |
| Q8b: Code licence — APPROVED | MIT licence | Owner approved MIT on 30 October 2026. Add the licence during authorised repository setup; confirm the copyright-holder name before filling its notice. Third-party dependencies/assets retain their own licence obligations. |
| Q8c: MVP distribution — APPROVED | Direct APK installation on the owner's device; Google Play publication deferred | Approved by owner on 30 October 2026. Manual APK updates initially. Public source visibility does not authorise public binary publication. Plan stable application identity and protected signing-key custody for updates during implementation. |
| Q8d: Repository identity — APPROVED | `QuantumFighter/gym-tracker-android` | Owner approved on 30 October 2026. Public visibility and MIT licence are separately approved. Repository creation and GitHub setup were subsequently authorised on 30 October 2026; Android implementation remains unauthorised. |

Q1–Q8 are resolved. Implement their approved behaviour without reopening these decisions. The consolidated technical baseline was accepted in final planning approval; application implementation still needs a separate explicit instruction.

## Approved defaults and workflow choices

Approved individually on 30 October 2026: completed workouts are read-only for the MVP. Saved sets can be corrected while the session remains active, including warm-down before Finish. Editing completed workouts is deferred to the later backlog; it is not part of MVP scope.

Approved individually on 30 October 2026: editing a shared routine shows affected programs and offers either Update Shared Routine (all references) or Make a Separate Copy (replace only the current program's reference). Previously completed and already-started workouts remain unchanged.

Approved individually on 30 October 2026: Home cycles through the ordered routines in the selected program, wrapping to the beginning. Finishing a workout advances the cycle; abandoning one does not. Manual routine selection remains available. Calendar-based scheduling is deferred.

Owner preference recorded on 30 October 2026: favour deferring optional enhancements to a later backlog or wish list to keep development focused. This does not remove confirmed initial-release requirements or override approved MVP scope. Merge restoration is a wish-list item; other scope reductions require explicit agreement.

| Default | Approved baseline |
| --- | --- |
| Units/weight controls | Metric initially (kg, metres/km); configurable weight increments and manual changes |
| Rest | 90 seconds initially, editable per exercise or superset |
| Completed exercises | Struck-through with explicit Complete status; persistent option to hide |
| Active session | One at a time; starting again resumes it; abandon retains saved sets with abandoned status |
| Program duplication | Independent copies of routines; editing the duplicate does not affect the source |
| Saving | Completed sets persisted before acknowledgement; unsubmitted draft controls best-effort |
| Warm-down | Optional timed phase; confirmation with pending exercises; return to recording before Finish |
| Estimates | Approx. 45 seconds per repetition-based set and 15 seconds per transition, plus prescribed rests |
| Reboot | Approximate recovered timer, visible uncertainty and correction; saved performance stays intact |
| Navigation | Home/Programs/Progress main destinations; Library/Settings secondary; focused Workout flow |
| Layout | Phone-first; dedicated tablet/foldable layouts deferred |
| Import limits | 25 MiB input and 100,000 sets; clear rejection without data changes above either limit |

The defaults bundle was approved by the owner on 30 October 2026. It is the implementation baseline, not a set of pending questions. Repetition-only progression ships before any duration/distance progression policy.

## Final review status and remaining setup details

All product decisions and defaults discussed with the owner are approved. No further product questionnaire is required for this planning stage. Final planning approval accepts the consolidated foundation, recommended architecture, technical acceptance targets and candidate sprint sequence as the development baseline. Sprint dates/capacity remain subject to actual kickoff refinement.

Final planning approval was granted by the owner on 30 October 2026. The owner separately authorised public GitHub repository/Project setup, issue import, templates and branch protection only. Android code, Sprint 1 implementation and application build workflows remain outside this setup scope.

Setup records the owner-supplied MIT copyright-holder name Reegan Milne and the local checkout C:\QUT\Projects\gym-tracker-android. Resolve compatible stable tool versions, application identifier, signing-key custody, notification delivery mechanism and concrete exercise/load metadata during the relevant implementation stories. These are bounded implementation/refinement tasks, not additional upfront product approvals. Escalate only if evidence requires changing approved behaviour or scope.

The [backlog wish list](BACKLOG.md#deferred-wish-list) is the canonical list of later enhancements. It has no dates or release commitments and does not dilute M1–M3 scope.

## Cross-document review record

- All confirmed product areas have FR IDs and backlog owners; NFR IDs also map to test scenarios.
- Generated/manual plans share a model; history never depends on mutable names/prescriptions.
- Supersets carry grouping and round state through recording, history, exports and progression.
- Same-boot timer precision is separated from reboot uncertainty and alert delivery.
- M1 deliberately lacks some M2 reliability/UX features; it is an internal demo, not advertised as the complete release.
- A single issue backlog replaces duplicate sprint/status spreadsheets after import.
- No unapproved app implementation, GitHub creation, workflow activation or deployment is included.

Open review risks: genuine exercise-content suitability and equipment load semantics require owner review; dependency/device compatibility and background behaviour require device experiments during implementation. Documentation checks cannot prove runtime behaviour.

## GitHub setup authorisation record

On 30 October 2026 the owner approved this consolidated foundation as the development baseline and explicitly requested GitHub setup only. MIT copyright holder: Reegan Milne. Local checkout: C:\QUT\Projects\gym-tracker-android. Existing product requirements are unchanged. B-01's repository-administration portion is prepared by setup, but its Android/CI work remains unimplemented and the issue stays open.
