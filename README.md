# Gym Tracker Android

Approved planning baseline • 30 October 2026 • **B-01 (#7) implementation authorised on 31 October 2026. Other stories remain unauthorised.**

A personal, local-first Android gym tracker designed around a minimal workout recording experience. The first functional milestone is to create a routine manually, record a complete workout and retrieve its persisted results offline.

This repository contains the approved planning foundation and the B-01 native Android foundation: Kotlin, Compose, Material 3 and Navigation 3 in one application module, with real build/lint/unit/Android smoke checks. The working product name is provisional. See [development setup and boundaries](docs/DEVELOPMENT.md) and [B-01 verification](docs/verification/B-01.md).

Repository: [QuantumFighter/gym-tracker-android](https://github.com/QuantumFighter/gym-tracker-android), public, MIT-licensed. Copyright 2026 Reegan Milne. The owner approved planning and GitHub setup on 30 October 2026, then explicitly authorised implementation of B-01 only on 31 October 2026.

## Document map

| Document | Purpose |
| --- | --- |
| [Product requirements](docs/PRODUCT_REQUIREMENTS.md) | Scope, priorities and verifiable requirement IDs |
| [Architecture](docs/ARCHITECTURE.md) | Components, boundaries, timers and official guidance |
| [Data model](docs/DATA_MODEL.md) | Editable plans, immutable history and portable data |
| [Testing strategy](docs/TESTING_STRATEGY.md) | Test catalogue, recovery, migrations and CI gates |
| [Agile process](docs/AGILE_PROCESS.md) | Solo workflow, GitHub setup and reusable PR template |
| [Release plan](docs/RELEASE_PLAN.md) | Milestones, two-week sprint objectives and risks |
| [Decisions](docs/DECISIONS.md) | Confirmed constraints, proposals and approval questions |
| [Product backlog](docs/BACKLOG.md) | Ordered issue-ready stories, acceptance criteria and dependencies |

## Direction

Use Kotlin, Android Studio, Compose with Material 3, ViewModel and StateFlow, Coroutines, repositories, Room/SQLite, Gradle Kotlin DSL, JUnit and Android UI tests. Use one [GitHub Project](https://github.com/users/QuantumFighter/projects/2) for Agile tracking. B-01 establishes the toolchain and checks; Room/schema/catalogue implementation begins with separately authorised B-02. There is no backend, login or cloud synchronisation in the initial release.

The proposed architecture is a single application module with clear UI, domain-rule and data packages. Persist workout actions transactionally. Snapshot routine prescriptions into sessions so later editing never rewrites history. Keep progression, superset rest policy and estimates independently testable.

Six conceptual areas remain available: Home, Programs, Workout, Progress, Exercise Library and Settings (the initial Account/Settings area has no account). Approved navigation uses Home, Programs and Progress as main destinations, Library/Settings through secondary navigation and a focused Workout flow.

## Scope and governance

Product decisions and the defaults bundle are approved and consolidated in [Decisions](docs/DECISIONS.md). The owner approved the consolidated planning foundation, including its architecture, quality targets and candidate sprint sequence, as the development baseline. Implementation authority currently covers B-01 only. MVP means the smaller manual-workout release; the full initial release also includes limited offline generation and duration/distance recording.

## Approved baseline summary

| Area | Approved direction |
| --- | --- |
| Delivery | M1 manual workout demonstration → M2 reliable personal-use MVP → M3 full initial scope |
| Device and distribution | Android 13+, Galaxy A32 / One UI 5.1 as primary test device; direct APK installation, manual updates |
| Workout | Free exercise order, round-based superset rest, screen-off rest alerts, active-session corrections and read-only completed workouts |
| Progression | One completed session; at least three prescribed working sets and all at upper rep targets. Two-set prescriptions use manual increases. Suggestions never apply silently. |
| Starting weights | Most recent completed comparable exercise performance, with date/source and override; no history means unset |
| Plans | Cycle through selected program; manual selection; shared edits or separate copy; independent program duplication |
| Data | User-initiated JSON/CSV without profile/account fields; guarded replace-all restore; no automatic OS backup/transfer where supported |
| Public project | `QuantumFighter/gym-tracker-android`, MIT; no personal workout data or secrets in the repository |

Review [Decisions](docs/DECISIONS.md) for the approved defaults and remaining administrative details, then [Release plan](docs/RELEASE_PLAN.md) and [Backlog](docs/BACKLOG.md) for delivery. The backlog contains 6 epics, 20 implementation stories and one explicitly uncommitted wish list. No approved initial-release feature has been moved to that wish list.

Requirement IDs (`FR-*`, `NFR-*`) are stable. Backlog keys (`B-*`) are temporary planning references, not invented GitHub issue numbers. Test scenarios (`T-*`) define evidence expected during implementation; the B-01 report distinguishes foundation checks from future full workout scenarios. GitHub is the only source of delivery status; the backlog is an index of imported issue links.

## Local opening and implementation handover

The local checkout is `C:\QUT\Projects\gym-tracker-android`. In Android Studio choose **Open** (or File → Open) and select that folder, then sync the Gradle project. Follow [Development](docs/DEVELOPMENT.md) for exact tool versions, SDK setup, build/test commands, Galaxy A32 installation and signing custody. Do not use New Project to create a second checkout.

For another computer, clone `https://github.com/QuantumFighter/gym-tracker-android.git`, then open the cloned folder. GitHub Issues and the linked Project hold live delivery status; the backlog document is the issue index and historical sprint plan.

The owner authorised B-01 on 31 October 2026. Keep dependent stories unstarted until prerequisites pass and the owner separately authorises them. The older planning documents retain their dated setup-only handover record; their approved product/architecture decisions remain authoritative. Project iteration dates remain provisional; no scheduled job starts development.

## GitHub setup result (30 October 2026 historical record)

- [Agile Project](https://github.com/users/QuantumFighter/projects/2): 6 epics, 20 stories, native sub-issues/dependencies, Priority/Status/Iteration/Estimate fields, Backlog/Sprint 1/Milestones views.
- Sprint 1: [B-01 #7](https://github.com/QuantumFighter/gym-tracker-android/issues/7) through [B-05 #11](https://github.com/QuantumFighter/gym-tracker-android/issues/11). None implemented.
- Three release milestones; eight provisional 14-day iterations; estimates unset until refinement.
- Pull requests, resolved conversations and linear history required on main; force pushes and branch deletion disabled. Solo workflow requires no second-person approval.
- Squash merge only. Real Android build/lint/unit/UI checks remain B-01 work, so no nonexistent checks or placeholder workflows are configured.
- Commit author email uses GitHub noreply. No personal workout data, credentials or signing material is included. GitHub setup scripts and downloaded tools are outside this repository.
