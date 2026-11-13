# Prompt for the local Android implementation assistant

Repository: QuantumFighter/gym-tracker-android.
Local checkout: C:\QUT\Projects\gym-tracker-android.

First inspect Git status and all applicable repository instructions. Do not discard changes or disturb another feature branch.

Read docs/ui/README.md, UI_SPECIFICATION.md, DECISION_CHANGES.md, IMPLEMENTATION_PLAN.md and VERIFICATION.md. Open prototype/index.html and capture/inspect the corresponding screenshots locally (none could be rendered in the packaging environment). Read the existing requirements, architecture, data model, decisions, testing strategy, agile process and relevant verification reports. Retrieve current issues/PRs; documentation setup-era status may be stale.

The user approved these visual designs, not wholesale implementation of all future features. For the first task, perform a read-only gap audit and propose focused issues. If separately instructed to import this package, use a documentation-only branch and PR before application implementation.

For each screen, report existing components/routes, layout mismatches, missing behaviour, relevant existing B-story, required domain decisions and a testable bounded increment. Explicitly reconcile warm-up countdown, individual-screen Warm Down removal, inline-only rest, chronological set corrections, extra-set opt-in, session superset unlink and exercise notes. Do not silently change transaction boundaries or history semantics.

Do not copy this JavaScript into Android, introduce a WebView, invent weights/durations or treat demo saves as persistence requirements. Preserve the native Compose/ViewModel/domain/Room architecture, durable save acknowledgement, idempotency and finalisation behaviour. M3 generator/non-repetition functionality remains release-gated.

Before implementing each separately authorised increment, provide its acceptance mapping and scope. Then perform appropriate checks, supply native screenshots with synthetic data, and seek owner visual review through a focused PR. Do not automatically merge or begin another story.
