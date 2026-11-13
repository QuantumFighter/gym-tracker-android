# Integrate approved warm-up, warm-down and inline rest timer UI

Related: B-06 (#12), B-09 (#15). Scope depends on existing timer baseline; do not replace durable timer architecture with web intervals.

- [ ] Warm-up preset/custom picker matches reference and starts countdown immediately.
- [ ] Custom trigger hides when entry opens; no Get Ready or extra Start action.
- [ ] Equal-width pause/play and green Work Out; full-screen Work Out at zero.
- [ ] Work Out navigates to exercise selection without finalising.
- [ ] Rest is centred inline, ±15 seconds and small black boxed filled play/pause; remove expand action.
- [ ] Warm-down is 10-minute countdown with ±1 minute and separate return/finish; no optional/partial labels.
- [ ] Reconcile countdown changes with persisted timer state, clock recovery and existing alert policies.
- [ ] Cancelling early-ending confirmation preserves running/paused state; expiry during confirmation is coherent.
- [ ] Apply domain input bounds, test timer adjustments and required process-death recovery on actual data.

Ten-second demo mode is not production UI. Confirm warm-down auto-start policy before implementation. No unrelated alert/reboot scope expansion or automatic merge.
