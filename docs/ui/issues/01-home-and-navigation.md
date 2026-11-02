# Align Home, shared typography and navigation with approved UI

Refine alongside B-09 (#15), avoiding duplicate tracked work. Related area: platform/workout. Dependencies: UI gap audit and existing navigation baseline.

As a trainee, I want immediate access to the next or active workout with minimal clutter.

- [ ] Match Home screenshot: soft background, large heading, circular Play, left-aligned metadata, Edit routine and Settings.
- [ ] Use real next-routine data; do not invent estimates before their implementation.
- [ ] Resume existing sessions rather than create duplicates.
- [ ] Connect Programs, Progress and Settings; make History a child of Progress.
- [ ] Implement completed visibility and other currently supported preferences without in-workout clutter.
- [ ] Add reviewed empty/loading/error states and accessible effective targets.
- [ ] Capture device screenshots and pass appropriate navigation/regression checks.

Out of scope: generation, new progression rules, storage architecture redesign. Resolve current requirement/test identifiers in refinement. Focused PR; no automatic merge.
