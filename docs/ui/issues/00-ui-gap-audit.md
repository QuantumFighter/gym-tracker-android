# Audit current Android screens against the approved UI baseline

Suggested labels: type:chore, area:platform. Proposed status: Backlog; no sprint assignment implied.

As the owner, I want a code-grounded gap map so the approved screens are implemented without duplicating existing functionality or destabilising persistence.

Read docs/ui and applicable repository instructions. Inspect current Compose screens, navigation, ViewModels, repositories, Room entities/migrations, tests and live backlog. Do not modify application code in this audit.

- [ ] Map all 20 reference screens to actual components/routes and existing B-stories.
- [ ] Classify each control as implemented, visual-only mismatch, missing behaviour, future release or unresolved decision.
- [ ] Reconcile DECISION_CHANGES with current approved requirements.
- [ ] Resolve exact requirement/test IDs from source documents rather than inventing them.
- [ ] Identify narrow increments, dependencies and required tests, preserving one-story WIP.
- [ ] Capture current screenshots using synthetic data and compare to reference.
- [ ] Report design approval separately from functional readiness.

Deliver docs/ui/GAP_AUDIT.md through a documentation-only PR. No merge or implementation of future features without the owner's instruction.
