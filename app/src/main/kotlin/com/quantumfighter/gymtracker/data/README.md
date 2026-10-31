# Data boundary

Repositories and Android storage/file/clock adapters belong here. Feature ViewModels
receive repositories through constructors; composables receive state and callbacks.
Repositories must not depend on Compose or feature UI. Wire implementations in `app/`.

B-02 adds `exercise/`: one Room schema-v1 exercise table, atomic idempotent catalogue
seeding and an injected `ExerciseRepository`. DAO/entity/database types are internal;
the public repository returns validated pure Kotlin exercise definitions and guards
recording selection. Catalogue metadata and storage contracts are documented in
`docs/EXERCISE_CATALOGUE.md`.

B-03 adds plan/cursor storage; B-04 adds session snapshots and retry-safe saves.
B-05 adds transactional completion, warm-down, corrections, finalisation and cursor
advancement. Finished history reads only session-owned records. Schema v4 preserves
all earlier data through explicit migrations; see `docs/DEVELOPMENT.md`.
