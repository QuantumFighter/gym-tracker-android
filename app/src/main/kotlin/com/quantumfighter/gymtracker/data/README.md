# Data boundary

Repositories and Android storage/file/clock adapters belong here. Feature ViewModels
receive repository interfaces through constructors; composables receive state and callbacks.
Repositories must not depend on Compose or feature UI. Wire implementations in `app/`.

B-01 has no persisted product data. Room, the first schema and catalogue seeding begin
in B-02 (#8), after separate authorisation. Do not add fake persistence or empty DAOs.
