# Solo Agile and GitHub workflow

Two-week iterations; scope follows actual available capacity. Planning is approved and GitHub setup is authorised. The repository and single Project implement these conventions; Android development has not started.

## Sprint rhythm

Before each sprint, refine the highest-priority stories and dependencies, resolve blocking product questions, and split anything larger than about two focused development days. Plan one demonstrable outcome and reserve about 20% of capacity for integration, defects and review. Story points are optional; use relative S/M/L sizing until real throughput exists. A solo developer does not need daily ceremony: maintain a brief progress/blocker note in the issue.

At the end, demonstrate acceptance scenarios against persisted data, review defects/risks, and record one practical retrospective improvement. Carry incomplete work forward visibly; do not label partial stories done. Reforecast after two sprints. A sprint is a timebox, not a promise that every listed story fits regardless of capacity.

## Single tracking system

Create one GitHub Project after approval. Fields: Status (Backlog, Ready, In progress, In review, Done), Priority (P0–P3), Iteration, Estimate (S/M/L), milestone and parent epic. Use native issue parent relationships where available; otherwise link the epic in the issue body. Requirement IDs and acceptance criteria stay in the body, not a second spreadsheet. Board views: current sprint, prioritised backlog and milestones. WIP limit: one implementation story, with one review/defect allowed concurrently.

Labels: `type:epic`, `type:story`, `type:bug`, `type:spike`, `type:chore`; `area:workout`, `area:programs`, `area:library`, `area:progress`, `area:data`, `area:platform`; `blocked`, `decision-needed`. Priority and iteration are fields, not duplicate labels. Milestones: M1 Manual Workout Slice, M2 Reliable Manual MVP, M3 Full Initial Release. On import, replace B-keys with actual issue links in an index; archive this document's planning order as baseline and maintain live order/status only in GitHub.

## Branching and PRs

Approved repository visibility is public (Q8a), with the MIT code licence approved under Q8b. Keep all personal workout datasets/backups, credentials and signing secrets out of source control and CI artifacts; use synthetic test fixtures. Repository creation is authorised for setup only. The MIT notice names Reegan Milne; retain applicable third-party notices.

Use protected `main` and short-lived `feat/<issue>-description`, `fix/...` or `docs/...` branches. No permanent develop branch. Each branch contains one independently reviewable increment; squash merge, then remove the branch. Link the issue and requirement IDs. Incomplete features may be hidden behind a local feature switch without breaking normal workflows.

Require PRs, passing named checks, resolved review conversations, and prevent force pushes/deletion on main. For a solo repository, do not require an unattainable approval from a second person; perform a deliberate self-review and use an independent reviewer when available. Require one external approval once a second maintainer exists. Avoid routine admin bypass. [GitHub branch protection documentation](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches) describes available controls and plan/visibility restrictions; verify the selected controls during setup of the approved public repository.

CI setup follows [Testing strategy](TESTING_STRATEGY.md). Workflow permissions should be read-only by default; pin third-party actions to reviewed commit SHAs and keep secrets out of pull-request jobs. Plan signing-key custody and direct APK installation/update verification under approved Q8c; keep Google Play publication deferred.

## Definition of Ready

- User value, acceptance criteria, requirement/test IDs and priority are clear.
- Dependencies and decisions are resolved or the story is explicitly a bounded spike.
- Validation and failure/recovery behaviour are stated; effort fits a small increment.
- UI behaviour is sketched where ambiguity would affect implementation; schema changes include migration intent.

## Definition of Done

- Acceptance criteria demonstrated; relevant regression checks pass in CI and evidence is linked.
- Error, offline, accessibility and recovery paths relevant to the story are covered.
- Schema/backup changes include migrations and preserved-data tests where applicable.
- Self-review completed; no debug payloads, credentials or personal data in source/logs.
- Requirement/decision documents updated for real behaviour changes, and issue closed by a merged PR.
- Builds remain runnable; known limitations are visible. No critical data-loss issue remains unresolved.

## Proposed PR template

Copy this into `.github/pull_request_template.md` only during authorised setup:

```markdown
## Problem and outcome
Describe the trigger and resulting behaviour.

## Traceability
Closes #<real issue>
Requirements: <FR/NFR IDs>
Acceptance scenarios: <T IDs>

## Validation
- Commands/checks and results:
- User-flow demonstration (if applicable):
- Recovery, migration or backup evidence (if applicable):

## Risks and decisions
Document behaviour changes, limitations and rollback/data considerations.

- [ ] Relevant acceptance criteria met
- [ ] Self-reviewed and required checks passed
- [ ] Documentation/schema changes included where needed
```

## Proposed issue body

Title: `[B-key] concise user outcome` during import only. Body: epic; “As a … I want … so that …”; requirement IDs; acceptance bullets; dependency issue links; scenario IDs; out-of-scope details and decisions. Copy the ready-made stories from [Backlog](BACKLOG.md); do not fabricate issue numbers. Epics are grouping records and close when their child stories satisfy the release gate, not standalone implementation tasks.

## Setup-stage enforcement

Require a PR to main, resolved review conversations and linear history; disallow force pushes/deletion. No external approval count is required for the solo owner. Squash merge is the only enabled merge strategy. The future build/lint/unit-tests/android-tests checks are intentionally not required or stubbed before B-01 implements runnable Android CI. Enable them after the first real checks report successfully, before merging application code. This avoids permanently pending required checks and false green placeholder workflows.

Two-week iterations use provisional dates from 2 November 2026. All issues remain unstarted; Sprint 1 membership is planning, not implementation authority. Keep later candidates in Backlog and replan at actual kickoff. Estimates remain unset until refinement rather than inventing effort.
