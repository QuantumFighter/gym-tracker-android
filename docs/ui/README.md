# Approved UI handover — Gym Tracker Android

Design baseline: 2 November 2026, Australia/Brisbane. Repository: QuantumFighter/gym-tracker-android.

This documentation package captures the owner's iterative screen approvals. It is a design reference for native Kotlin/Compose implementation, not replacement application code or authorisation to implement every future story.

## Start here

1. Open `prototype/index.html` in a modern browser. No server or build is required.
2. Press Play on Home, or use the preview-only screen selector. Demo timers default to ten seconds.
3. Read [UI_SPECIFICATION.md](UI_SPECIFICATION.md), [DECISION_CHANGES.md](DECISION_CHANGES.md), and [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md).
4. Compare the native app with the prototype and locally captured screenshots. Use [ACCEPTANCE_CHECKLIST.md](ACCEPTANCE_CHECKLIST.md) for review.
5. Read [VERIFICATION.md](VERIFICATION.md) for executed checks and prototype limitations.

Screenshot capture instructions and a script are included. PNGs could not be produced in this environment; see screenshots/README.md and VERIFICATION.md. The script uses synthetic data, not the owner's workout records.

## Authority and scope

The written specification records intentional decisions; screenshots show visual hierarchy; the prototype demonstrates navigation. Browser behaviour that conflicts with the written data-integrity requirements is a prototype defect, not an approved requirement. Earlier chat issue drafts are superseded where DECISION_CHANGES explicitly records later feedback.

Preserve Room data, transactional recording, migration history, recovery and the approved Android architecture. Do not convert the app to a WebView or copy JavaScript timers into production. No source-code changes, commits, PRs or GitHub issues were made by preparing this package.

## Contents

- `prototype/index.html`: standalone, portable browser preview.
- `prototype/source.html`: editable source fragment from the approved click-through, with portability/runtime fixes documented in VERIFICATION.
- `prototype/assets/`: font assets and third-party notices.
- `screenshots/`: capture instructions; the script generates all 20 primary views plus selected expanded states locally.
- `UI_SPECIFICATION.md`: screen-by-screen requirements and styling.
- `DECISION_CHANGES.md`: explicit changes from earlier plans and unresolved details.
- `IMPLEMENTATION_PLAN.md`: proposed small implementation increments and known backlog mappings.
- `issues/`: ready-to-paste issue drafts, not created issues.
- `ACCEPTANCE_CHECKLIST.md`: walkthrough and regression expectations.
- `CODEX_HANDOFF.md`: prompt for the local implementation assistant.

## Import into the repository

Extract the ZIP and copy its `docs/ui/` directory into your local checkout. Inspect existing files before overwriting. Do not copy the ZIP itself into Git.

From `C:\QUT\Projects\gym-tracker-android`, inspect `git status`, then use a documentation branch, for example:

```powershell
git switch -c docs/approved-ui-handover
git add docs/ui
git diff --cached --stat
git commit -m "docs: add approved UI prototype and implementation handover"
git push -u origin docs/approved-ui-handover
```

Start from the appropriate up-to-date baseline after checking repository instructions; do not discard unrelated changes. Open a documentation-only PR using `PR_DESCRIPTION.md`. Review before merging. Add issues through the existing GitHub Project rather than creating another tracking system.

## Editing the portable export

`source.html` is the editable source. `index.html` contains a rendered iframe export, so editing it directly is less convenient. A standalone fallback rebuild script is included at `prototype/rebuild.py`; it builds a plain browser document from the source with embedded local icons/fonts and no ChatGPT host APIs. The packaged index was verified as delivered; rerun browser checks after rebuilding or editing. Browser storage is not durable application storage.
