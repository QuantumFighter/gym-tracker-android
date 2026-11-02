# Add approved exercise selection, multi-select and superset unlink

Related B-08 (#14), B-09 (#15), B-10 (#16). Refine domain mutations before implementation.

- [ ] Main routine title, progress bar/cards and bottom Warm Down match reference.
- [ ] Normal tap opens recording; hold selects, further taps toggle multi-selection; accessible alternative exists.
- [ ] Remove and Superset actions appear; group requires at least two members.
- [ ] Holding grouped exercise changes action to broken-link Unlink.
- [ ] Unlink preserves exercise/sets; dissolving a two-member group returns both to ordinary exercises.
- [ ] Define mixed selections, mid-round changes and orphan-group prevention; preserve already-recorded group context.
- [ ] Confirm current-session removal, retain saved sets, define skipped/partial and progress denominator semantics.
- [ ] Implement progress/remaining estimate from real data only and respect completed-visibility preference.
- [ ] Verify group changes, repeated commands, interruption, saved data and rest-boundary regressions.

Do not interpret prototype in-memory filtering as approved database deletion. Split UI and domain changes where appropriate. No automatic merge.
