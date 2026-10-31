# Data boundary

Repositories and Android storage/file/clock adapters belong here. Feature ViewModels
receive repositories through constructors; composables receive state and callbacks.
Repositories must not depend on Compose or feature UI. Wire implementations in `app/`.

B-02 adds `exercise/`: one Room schema-v1 exercise table, atomic idempotent catalogue
seeding and an injected `ExerciseRepository`. DAO/entity/database types are internal;
the public repository returns validated pure Kotlin exercise definitions and guards
recording selection. Catalogue metadata and storage contracts are documented in
`docs/EXERCISE_CATALOGUE.md`. No program, routine or workout persistence is implemented.
