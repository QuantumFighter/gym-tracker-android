# Product backlog — GitHub issue index

Approved on 30 October 2026 and imported into GitHub. [Project](https://github.com/users/QuantumFighter/projects/2) owns live status, priority, iteration and estimates; issue bodies own acceptance criteria and native dependency/epic links. This file is a stable reference index, not a second status tracker. All implementation was unstarted at setup; no story was closed by repository administration.

The complete pre-import issue-ready backlog is preserved in [baseline commit cd6003b](https://github.com/QuantumFighter/gym-tracker-android/blob/cd6003b/docs/BACKLOG.md). Product requirements and acceptance criteria were not expanded during import.

## Epics

| Planning key | GitHub issue | Child stories |
| --- | --- | --- |
| E-01 | [#1 — Platform and integrity](https://github.com/QuantumFighter/gym-tracker-android/issues/1) | [B-01](https://github.com/QuantumFighter/gym-tracker-android/issues/7), [B-06](https://github.com/QuantumFighter/gym-tracker-android/issues/12), [B-07](https://github.com/QuantumFighter/gym-tracker-android/issues/13), [B-16](https://github.com/QuantumFighter/gym-tracker-android/issues/22), [B-17](https://github.com/QuantumFighter/gym-tracker-android/issues/23), [B-20](https://github.com/QuantumFighter/gym-tracker-android/issues/26) |
| E-02 | [#2 — Manual plans and library](https://github.com/QuantumFighter/gym-tracker-android/issues/2) | [B-02](https://github.com/QuantumFighter/gym-tracker-android/issues/8), [B-03](https://github.com/QuantumFighter/gym-tracker-android/issues/9), [B-11](https://github.com/QuantumFighter/gym-tracker-android/issues/17), [B-12](https://github.com/QuantumFighter/gym-tracker-android/issues/18) |
| E-03 | [#3 — Workout execution](https://github.com/QuantumFighter/gym-tracker-android/issues/3) | [B-04](https://github.com/QuantumFighter/gym-tracker-android/issues/10), [B-05](https://github.com/QuantumFighter/gym-tracker-android/issues/11), [B-08](https://github.com/QuantumFighter/gym-tracker-android/issues/14), [B-09](https://github.com/QuantumFighter/gym-tracker-android/issues/15), [B-10](https://github.com/QuantumFighter/gym-tracker-android/issues/16) |
| E-04 | [#4 — Progression](https://github.com/QuantumFighter/gym-tracker-android/issues/4) | [B-13](https://github.com/QuantumFighter/gym-tracker-android/issues/19) |
| E-05 | [#5 — Data portability](https://github.com/QuantumFighter/gym-tracker-android/issues/5) | [B-14](https://github.com/QuantumFighter/gym-tracker-android/issues/20), [B-15](https://github.com/QuantumFighter/gym-tracker-android/issues/21) |
| E-06 | [#6 — Full initial scope](https://github.com/QuantumFighter/gym-tracker-android/issues/6) | [B-18](https://github.com/QuantumFighter/gym-tracker-android/issues/24), [B-19](https://github.com/QuantumFighter/gym-tracker-android/issues/25) |

## Stories and traceability

Priority and sprint columns below record the approved import baseline only. Change delivery planning in the Project, not this table. Milestone links represent release gates without fixed due dates.

| Key / issue | Parent | Milestone | Initial priority / sprint | Requirements | Tests |
| --- | --- | --- | --- | --- | --- |
| [B-01: Establish the approved Android foundation](https://github.com/QuantumFighter/gym-tracker-android/issues/7) | [E-01](https://github.com/QuantumFighter/gym-tracker-android/issues/1) | [M1](https://github.com/QuantumFighter/gym-tracker-android/milestone/1) | P0 / 1 | NFR-01, NFR-06 | T-01, T-15 |
| [B-02: Persist a small built-in exercise catalogue](https://github.com/QuantumFighter/gym-tracker-android/issues/8) | [E-02](https://github.com/QuantumFighter/gym-tracker-android/issues/2) | [M1](https://github.com/QuantumFighter/gym-tracker-android/milestone/1) | P0 / 1 | FR-12, NFR-01, NFR-03 | T-01, T-11 |
| [B-03: Create a manual program and reusable routine](https://github.com/QuantumFighter/gym-tracker-android/issues/9) | [E-02](https://github.com/QuantumFighter/gym-tracker-android/issues/2) | [M1](https://github.com/QuantumFighter/gym-tracker-android/milestone/1) | P0 / 1 | FR-02 | T-01, T-02 |
| [B-04: Start and autosave an out-of-order workout](https://github.com/QuantumFighter/gym-tracker-android/issues/10) | [E-03](https://github.com/QuantumFighter/gym-tracker-android/issues/3) | [M1](https://github.com/QuantumFighter/gym-tracker-android/milestone/1) | P0 / 1 | FR-03, FR-04, FR-17, FR-18, NFR-01, NFR-02 | T-01, T-02, T-06, T-07 |
| [B-05: Warm down, finish and read saved history](https://github.com/QuantumFighter/gym-tracker-android/issues/11) | [E-03](https://github.com/QuantumFighter/gym-tracker-android/issues/3) | [M1](https://github.com/QuantumFighter/gym-tracker-android/milestone/1) | P0 / 1 | FR-07, FR-18, FR-21 | T-01, T-03, T-06, T-07 |
| [B-06: Recover sessions, durable timers and screen-off rest alerts](https://github.com/QuantumFighter/gym-tracker-android/issues/12) | [E-01](https://github.com/QuantumFighter/gym-tracker-android/issues/1) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 2 | FR-05, FR-17, NFR-02, NFR-08 | T-04, T-06 |
| [B-07: Prove migrations and storage failure recovery](https://github.com/QuantumFighter/gym-tracker-android/issues/13) | [E-01](https://github.com/QuantumFighter/gym-tracker-android/issues/1) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 2 | NFR-03, NFR-09 | T-02, T-06, T-11 |
| [B-08: Record supersets with configurable rest](https://github.com/QuantumFighter/gym-tracker-android/issues/14) | [E-03](https://github.com/QuantumFighter/gym-tracker-android/issues/3) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 3 | FR-05, FR-06, FR-16 | T-04, T-05 |
| [B-09: Refine minimal navigation and persistent visibility](https://github.com/QuantumFighter/gym-tracker-android/issues/15) | [E-03](https://github.com/QuantumFighter/gym-tracker-android/issues/3) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 3 | FR-01, FR-04, FR-08, NFR-04 | T-03, T-14 |
| [B-10: Estimate workout and remaining duration](https://github.com/QuantumFighter/gym-tracker-android/issues/16) | [E-03](https://github.com/QuantumFighter/gym-tracker-android/issues/3) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 3 | FR-09 | T-10 |
| [B-11: Edit, duplicate, archive and reuse plans safely](https://github.com/QuantumFighter/gym-tracker-android/issues/17) | [E-02](https://github.com/QuantumFighter/gym-tracker-android/issues/2) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 4 | FR-02, FR-10, FR-18 | T-07 |
| [B-12: Add custom exercises and substitutions](https://github.com/QuantumFighter/gym-tracker-android/issues/18) | [E-02](https://github.com/QuantumFighter/gym-tracker-android/issues/2) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 4 | FR-10, FR-12, FR-18 | T-02, T-07 |
| [B-13: Review history and explain progression](https://github.com/QuantumFighter/gym-tracker-android/issues/19) | [E-04](https://github.com/QuantumFighter/gym-tracker-android/issues/4) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 4 | FR-14, FR-15, FR-16, FR-21, NFR-06 | T-08, T-09 |
| [B-14: Export and restore complete JSON backups](https://github.com/QuantumFighter/gym-tracker-android/issues/20) | [E-05](https://github.com/QuantumFighter/gym-tracker-android/issues/5) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 5 | FR-19, NFR-07, NFR-09 | T-12, T-13, T-15 |
| [B-15: Export useful and safe CSV history](https://github.com/QuantumFighter/gym-tracker-android/issues/21) | [E-05](https://github.com/QuantumFighter/gym-tracker-android/issues/5) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 5 | FR-20 | T-13 |
| [B-16: Verify accessibility, performance and offline privacy](https://github.com/QuantumFighter/gym-tracker-android/issues/22) | [E-01](https://github.com/QuantumFighter/gym-tracker-android/issues/1) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 6 | NFR-01, NFR-04, NFR-05, NFR-07 | T-03, T-14, T-15 |
| [B-17: Qualify the manual MVP](https://github.com/QuantumFighter/gym-tracker-android/issues/23) | [E-01](https://github.com/QuantumFighter/gym-tracker-android/issues/1) | [M2](https://github.com/QuantumFighter/gym-tracker-android/milestone/2) | P1 / 6 | NFR-02, NFR-03, NFR-06 | T-01–T-15 |
| [B-18: Generate editable programs offline](https://github.com/QuantumFighter/gym-tracker-android/issues/24) | [E-06](https://github.com/QuantumFighter/gym-tracker-android/issues/6) | [M3](https://github.com/QuantumFighter/gym-tracker-android/milestone/3) | P2 / 7 | FR-11, FR-14 | T-09, T-16 |
| [B-19: Record duration and distance exercises](https://github.com/QuantumFighter/gym-tracker-android/issues/25) | [E-06](https://github.com/QuantumFighter/gym-tracker-android/issues/6) | [M3](https://github.com/QuantumFighter/gym-tracker-android/milestone/3) | P2 / 8 | FR-13 | T-02, T-10, T-12, T-13, T-17 |
| [B-20: Qualify the full initial release](https://github.com/QuantumFighter/gym-tracker-android/issues/26) | [E-01](https://github.com/QuantumFighter/gym-tracker-android/issues/1) | [M3](https://github.com/QuantumFighter/gym-tracker-android/milestone/3) | P2 / 8 | FR-11, FR-13, NFR-01–NFR-09 | T-01–T-17 |

## Sprint 1 backlog

The [Sprint 1 board](https://github.com/users/QuantumFighter/projects/2/views/2) contains B-01–B-05 (#7–#11). Objective: create a manual routine, record and finish a complete offline workout, then relaunch and retrieve exact persisted results. B-01 is Ready for future authorised work; B-02–B-05 remain in Backlog with native blockers. Ready is not In progress. B-01 still requires application/build/CI work; the setup portion alone does not satisfy its acceptance criteria.

Iteration dates are provisional: Sprint 1 is 2–15 November 2026 (Australia/Brisbane), followed by seven consecutive 14-day iterations reflecting the candidate sequence. Reset dates and refine capacity at actual kickoff. No scheduled automation starts development. Later-sprint assignments are candidates, not delivery commitments. Estimates remain unset until refinement.

## Deferred wish list

These items are outside the initial-release commitment. Do not schedule them, implement them or create a second delivery tracker for them by default. Promote an item to an issue only when scope is explicitly accepted.

| Later enhancement | Boundary / prerequisite |
| --- | --- |
| Editing completed workouts | MVP permits corrections only before Finish; future editing needs progression/history correction rules |
| Merge restoration | MVP replaces all with approved safeguards; future merge needs duplicate/conflict handling |
| Password-protected backups | MVP uses user-initiated plaintext exports excluding profile/account fields |
| Account/profile functionality and export | Outside initial release; profile/account export requires encryption and separate approval |
| Advanced generation/personalisation | M3 uses the approved four inputs and reviewed offline templates; adaptive programming deferred |
| Advanced starting-weight reliability | MVP uses most recent comparable performance; recency/consistency scoring deferred |
| Calendar routine scheduling | MVP uses selected-program cycling and manual choice |
| Dedicated tablet/foldable layouts | Phone-first initial release |
| Google Play publication | MVP is directly installed APK with manual updates |
| Advanced analytics and external integrations | Cloud sync, wearables and social features require separate scope; no implicit backend work |

Automatic load reductions and medical advice are excluded behaviours, not promised future enhancements.

## Traceability maintenance

A changed requirement updates its owner issue and affected T-scenario. Bugs link the original requirement and a regression. Preserve FR/NFR, B and T identifiers when refining issues. Requirements remain in PRODUCT_REQUIREMENTS.md and scenarios in TESTING_STRATEGY.md; avoid copying live status into Markdown. Deferred wish-list items were not converted to scheduled issues.
