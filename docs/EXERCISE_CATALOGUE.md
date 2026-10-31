# Built-in exercise catalogue v1

B-02 / [Issue #8](https://github.com/QuantumFighter/gym-tracker-android/issues/8).
Six deliberately small repetition-based definitions support the next manual-routine
story. No routines, prescriptions, recorded sets, substitutions or custom-exercise
editor are supplied here. This is descriptive metadata, not a training recommendation.

## Contents and content review

All IDs below are literal UUIDs committed in `BuiltInCatalogue.kt`; names are never
keys. Every entry has origin `BUILT_IN`, catalogue version `1`, exercise type
`strength`, measurement `REPETITIONS` and `archived = false` on first insertion.
Descriptions explicitly explain the load input. One repetition counts the complete
movement; the dumbbell press counts both arms together, with equal dumbbell masses.

| UUID | Name | Muscle tags | Equipment tags | Movement tag | Load |
| --- | --- | --- | --- | --- | --- |
| `6ae90adc-270d-4af1-ad4e-451410de1001` | Barbell back squat | quadriceps, glutes | barbell, rack | squat | TOTAL |
| `6ae90adc-270d-4af1-ad4e-451410de1002` | Barbell bench press | chest, triceps | barbell, bench | horizontal_push | TOTAL |
| `6ae90adc-270d-4af1-ad4e-451410de1003` | Barbell Romanian deadlift | hamstrings, glutes | barbell | hip_hinge | TOTAL |
| `6ae90adc-270d-4af1-ad4e-451410de1004` | Dumbbell overhead press | shoulders, triceps | dumbbells | vertical_push | PER_HAND |
| `6ae90adc-270d-4af1-ad4e-451410de1005` | Push-up | chest, triceps | bodyweight | horizontal_push | BODYWEIGHT |
| `6ae90adc-270d-4af1-ad4e-451410de1006` | Machine-assisted pull-up | back, biceps | assisted_pull_up_machine | vertical_pull | ASSISTANCE |

Implementation self-review checked identity uniqueness, required metadata, repetition
format and the description/load pairing against the table. Automated fixtures pin
all six UUIDs and conventions. Owner review of exercise suitability remains part of
PR review; no external training or clinical review is claimed.

## Measurement and load contract

- `TOTAL`: kg of all external load, including the bar and every plate. Not per side.
- `PER_HAND`: kg of one dumbbell, not the sum of both hands. No unilateral/asymmetric
  prescription is introduced by this catalogue.
- `BODYWEIGHT`: unloaded movement; no body mass, profile field or external-load input.
- `ASSISTANCE`: nonnegative kg indicated by the assistance machine. More assistance
  makes the movement easier; never encode assistance as negative weight or apply
  ordinary upward-load progression. Machine indications are equipment-specific and
  must not imply comparable loads across machines.

The catalogue stores **no measured loads**, starting weights or progression increments.
Later recording must distinguish unset from zero, store canonical integer grams,
retain display units and these conventions, and validate positive repetitions.
These requirements do not authorise recording or progression implementation in B-02.
Duration/distance tokens exist only to reject unsupported selections until B-19.
Both the repository list and lookup-by-ID guard against unsupported/archived entries.
Future recording entry points must use this boundary and validate again on save.

## Storage, seeding and change policy

Room schema version `1` has exactly one `exercises` table. The UUID text primary key
is unique/non-null; every field except `catalogueVersion` is non-null. Names are
deliberately not unique. No relationships or speculative indexes are needed for six
rows. The generated JSON is committed under `app/schemas/` and packaged in Android
test assets. The Room plugin exports each variant to the shared schema directory.

Enum names are explicit storage tokens. Metadata lists use `|`-separated unique
lowercase underscore tags, validated by the pure Kotlin `Exercise` model; tags cannot
contain the delimiter. This avoids unused taxonomy tables. UUID shape, nonblank text,
list/tag validity and origin/version pairing are application invariants, not SQLite
CHECK constraints. The internal DAO has no public write API; current writes map
validated domain definitions. Raw malformed data fails domain conversion rather than
being repaired or erased. Future custom/import writers must validate through the same
model and transaction boundary. Unknown enum tokens fail closed.

Home's ViewModel requests seeding through the injected repository. Every repository
read also seeds, so a later consumer never depends on Home having run first. Room's
suspend queries/transaction executor keep disk I/O off the main thread. One transaction
inserts missing UUIDs using ABORT, preserves every existing built-in field, and rejects
a custom/built-in identity collision with rollback. There is no replace/upsert,
delete-and-reinsert, global seeded preference or asynchronous database callback race.
Reopening/restarting is safe; concurrent calls serialize through Room transactions.

Catalogue version and database schema version are separate. No built-in metadata
update mechanism is implemented: future updates need explicit compatibility/preservation
rules. Retire used entries by archiving rather than recycling IDs or deleting lineage.
Custom origin/null catalogue version is representable for later B-12, with no custom
creation feature now. Subsequent sessions must snapshot definitions instead of joining
live metadata for historical display, as required by the approved data model.

B-01 had no database, so there is no schema-v0 migration. Future schema changes must
increment the Room version and supply tested migrations from committed v1. Production
has no destructive migration fallback and no main-thread-query allowance. Failed opens
show a generic retry state without logging payloads or deleting the file. Full damaged
storage recovery/export UX remains B-07 and later portability work.

## Sources and licensing

Names identify common movements. Descriptions, metadata selection and tag assignments
were written originally for this project; no third-party exercise dataset, prose,
image, video or asset was copied. These source definitions and documentation use the
repository's [MIT licence](../LICENSE), retaining its existing copyright notice.
No third-party exercise-content attribution or network download is required.

Room/AndroidX are Apache-2.0 dependencies, separately licensed from catalogue content.
Dependency versions were checked on 31 October 2026 against the official
[Room releases](https://developer.android.com/jetpack/androidx/releases/room),
[KSP release 2.3.13](https://github.com/google/ksp/releases/tag/2.3.13) and
[KSP setup](https://kotlinlang.org/docs/ksp-quickstart.html). Room runtime/compiler/plugin/
testing use stable `2.8.5`; KSP uses stable `2.3.13`. Compatibility is verified by the
project's actual compilation/tests. The existing B-01 AGP, Kotlin, Gradle, JDK and
Compose pins are retained. Serialization runtime BOM `1.12.0` aligns app/test core
and JSON after an actual schema-helper `AbstractMethodError` exposed incompatible
transitive versions. Its [official release](https://github.com/Kotlin/kotlinx.serialization/releases/tag/v1.12.0)
is based on Kotlin 2.4.10 and compatible with the retained 2.4.21 compiler.
