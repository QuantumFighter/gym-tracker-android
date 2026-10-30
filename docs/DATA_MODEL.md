# Data model and integrity

Logical model, not SQL or implemented entities. Use UUID identifiers suitable for offline export/import, UTC instants plus recorded local offset for historical display, and explicit schema/format versions. Names are never keys.

## Editable catalogue and plans

| Entity | Essential fields and relationships |
| --- | --- |
| Exercise | ID, built-in/custom origin, catalogue version, name, description, muscle groups, equipment, movement patterns, exercise type, measurement kind, load convention, archived flag |
| ExerciseSubstitution | Directed source/target exercise IDs, rationale; both must exist, no self-reference; compatibility validated when selected |
| Program | ID, name, notes, archived flag, manual/generated origin, generator version/inputs/reasons where applicable |
| Routine | ID, name, notes; independent reusable definition |
| ProgramRoutine | ID, program ID, routine ID, ordinal; unique ordinal per program; supports shared routine membership |
| RoutineExercise | ID, routine ID, exercise ID, ordinal, optional group ID; duplicate occurrences of one exercise have distinct IDs |
| RoutineGroup | ID, routine ID, group kind `superset`, ordered member references, round rest policy |
| SetPrescription | ID, routine-exercise ID, ordinal, working/warm-up type, rep minimum/maximum, optional load, duration/distance target, rest seconds |
| ProgressionPolicy | ID, optional routine-exercise override, mode, qualifying-session count, threshold, load increment and units; default policy in settings |
| AppSettings | Singleton: display units, completed-exercise display, default rest/steps, progression defaults, selected program, next ProgramRoutine ID and estimate assumptions |

Program duplication deep-copies routines, groups and prescriptions by default; within the copy retain intentional internal reuse. Routine edits affect programs referencing that routine; show affected program names and offer Update Shared Routine or Make a Separate Copy, which changes only the current program reference. Archiving removes a program from new-session selection without deleting its definitions/history. Built-in catalogue updates never overwrite user custom exercises. Archive used catalogue entries instead of hard-deleting them.

## Session-owned history

| Entity | Essential fields and relationships |
| --- | --- |
| WorkoutSession | ID, nullable source program/routine IDs, copied names, start/end instants and offsets, state, warm-up/down elapsed durations, estimate inputs/version |
| SessionExercise | ID, session ID, original exercise ID as lineage, snapshot name/metadata/measurement/load convention, source occurrence ID, ordinal, status, substitution provenance |
| SessionGroup | ID, session ID, snapshot group kind, member session-exercise IDs/order, rest policy |
| SessionPrescription | ID, session-exercise ID, copied set targets, warm-up/working type, rest and progression policy; immutable after session creation except explicit session-only changes with recorded provenance |
| RecordedSet | ID, session-exercise ID, command UUID, prescription reference if applicable, sequence, warm-up/working type, actual load/reps/duration/distance, original display units, completion time, optional group-round ID |
| SessionRound | ID, session-group ID, round ordinal, participating members, exclusions and rest-issued marker; supports unequal set counts |
| TimerState | ID, session ID, kind, state, anchors, boot evidence, accumulated/remaining duration, alert identity/revision, adjustment and uncertainty metadata; at most one active rest timer per session |
| RecommendationDecision | ID, session-exercise ID, policy version, recommendation/reasons, evidence session IDs, proposed value, accepted/overridden/manual outcome and chosen value |

Program 1→many ProgramRoutine; Routine 1→many ProgramRoutine and RoutineExercise; RoutineExercise 1→many SetPrescription. WorkoutSession 1→many SessionExercise/SessionGroup; SessionExercise 1→many SessionPrescription/RecordedSet. SessionGroup 1→many SessionRound. No historical display depends on live catalogue or routine values. Source IDs are optional lineage, never a requirement to reconstruct history.

Session start snapshots all required data transactionally. Changes to templates then affect only future sessions. Session substitution copies the replacement metadata, records original intent and preserves any existing recorded sets under their original SessionExercise; never relabel completed performance. New work uses a distinct session occurrence if sets were already recorded. Snapshot supersets retain the actual grouping used at the time.

## Constraints and transactions

- Store mass and distance as integer canonical subunits (proposed grams/millimetres), duration as integer milliseconds, reps as integers. Retain entered display unit and load convention (total/per-hand/bodyweight/assistance). Define conversion/rounding centrally; do not use binary floating point as storage truth.
- Validate measurement-kind field combinations. Repetition entries need positive reps, nonnegative external load where applicable; an unset load is distinct from zero. Duration/distance entries need positive measured values. Assisted/bodyweight semantics must be explicit; no unexplained negative loads.
- Unique `(sessionExercise, sequence)` and command UUID prevent duplicate persistence. Index session date, exercise lineage, foreign keys and progression lookup fields. Enforce ordinal uniqueness within parents.
- Save Set transaction includes the set, round completion and timer replacement. Edit/delete during active recording recomputes derived progress without duplicating timers. Finalisation and program cursor advance share a transaction.
- Enforce one active session using a singleton active-session reference plus repository serialisation/transaction checks. Clear it only when finishing/abandoning.
- Template foreign keys use restrictions or nullable lineage; never cascade template deletion into session history. Session-owned children may cascade only when an explicit future history-deletion feature is approved.
- Finalised history is read-only in MVP. Post-completion correction is a deferred backlog enhancement; do not silently mutate records via recalculation or migration.

## Backup/import contract

Approved privacy boundary (Q5b): export no user profile or account data. Use an explicit export-field allowlist, not automatic serialisation of every future database field. Include workout/program/library records and non-identifying app preferences; exclude names identifying the user, email addresses, birth dates, profile/body measurements, account identifiers and credentials if such fields are ever introduced. Exercise/program names and user-entered workout text remain workout data and may contain personal information, so warn users that exported files are private and readable by anyone holding them. No public upload is implied or authorised.

“Complete backup” means complete restoration of the supported MVP workout data and non-identifying preferences, not a future profile/account store. Profile/account data is outside this format; validate imports against the supported schema and reject unsupported profile/account sections. Any later profile export requires encryption, a revised format, tests and separate approval. Accounts/profile functionality and encrypted profile export are outside the initial release.

JSON envelope: format version, app/schema versions, export instant, catalogue version and all allowlisted workout/program/library collections including used built-in definitions, custom exercises, substitutions, complete programs/routines, non-identifying app preferences, history, active session, timers and recommendation decisions. Derived caches may be rebuilt; recorded inputs and provenance may not. Export a consistent read snapshot. Successful output means the document stream closed successfully; a cancelled or failed provider write is not a completed backup.

Approved restore mode (Q5a, 30 October 2026) is **replace all**, requiring user confirmation after a preview of counts/date and an offer to export current data. Merge restoration is a later wish-list item with no release commitment. Read a bounded file, validate envelope/IDs/references/enums/ranges/uniqueness, reject future incompatible formats, migrate supported older formats in staging, then replace all authoritative Room data in one transaction. Cancellation/error before commit leaves the original database intact. Failures after commit report that restore succeeded even if a subsequent UI refresh fails; do not claim rollback. No partial replace or silent duplicate-ID overwrite.

Restored active timers use imported wall-time evidence with uncertainty; device-local monotonic anchors are invalid on another device. Completed sets remain exact. Import cannot create two active sessions. Do not automatically merge bundled catalogue updates into a restored snapshot until compatibility is established. Plaintext export privacy is shown before file selection. Encryption is deferred to the later wish list; profile/account exports remain prohibited until encryption and separate approval are in place (Q5b).

## CSV contract

Propose one row per recorded set, plus an explicit record-kind `exercise_status` row for exercises with no sets. Columns: record kind, session/exercise/set IDs, recorded local date/offset and UTC time, session/exercise status, snapshot name, measurement kind/load convention, working/warm-up flag, actual measurements and units, prescription targets, group/round IDs and substitution origin. Use UTF-8 with documented quoting, decimal dot and empty absent values. Protect user text beginning with formula-triggering characters after leading whitespace; document the export transformation. CSV is not round-trip restore.

## Migrations and damaged storage

Commit exported Room schemas from the first schema version. Each release adds a migration path and fixtures from every supported released version. Validate both schema and meaningful record contents, foreign keys and counts. Backups have separate format migrations. Do not depend on copying an open SQLite file as a consistent backup.

On database-open failure, stop writes and show recovery options without resetting the file. Preserve the original for diagnosis/recovery; avoid displaying or logging raw data. An approved JSON restore can recover service; export only if safe reads are possible. Interrupted migration/open tests must demonstrate either valid old data or valid upgraded data, not an empty replacement.
