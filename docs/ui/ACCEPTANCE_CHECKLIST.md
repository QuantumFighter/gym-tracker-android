# Acceptance checklist

## Visual reference review

- [ ] Match approved hierarchy, palette, large text and compact navigation; adapt rather than copy CSS pixels.
- [ ] Test Galaxy A32, narrower width, larger system text and dark mode.
- [ ] Accessible labels, focus, screen-reader actions and non-overlapping 48 dp touch targets.
- [ ] Loading/empty/error/unavailable features are explicit; no foundation/debug text on Home.

## Workout walkthrough with synthetic data

- [ ] Start one routine; warm-up preset starts countdown; custom entry hides its trigger.
- [ ] Pause/resume/adjust supported timers; expiry transition does not finalise workout.
- [ ] Select exercises out of order; partial work remains visible after switching.
- [ ] Save and correct a set, including a middle set; correction sits in chronological order.
- [ ] At target count, only small + offers another set; correction does not duplicate data or restart rest.
- [ ] Hold/select/group/unlink; two-member group dissolves after one unlink; recording remains intact.
- [ ] Notes edit/save/cancel works against intended exercise identity.
- [ ] Warm Down is absent from individual recording; selection offers it and confirms unfinished work.
- [ ] Warm-down countdown starts at 10 minutes, ± adjusts one minute; return preserves saved work.
- [ ] Finish is transactional with program advancement and active-session removal; repeated finish is safe.
- [ ] Summary/history show exact sets and completion distinctions after relaunch, independent of later template edits.

## Production integrity, not browser-prototype assertions

- [ ] Save failure retains attempted input and last committed value; retry is idempotent.
- [ ] No success acknowledgement before database commit.
- [ ] Save/correction/finalise races have explicit deterministic outcomes.
- [ ] Required process-death/timer recovery and schema migrations tested against real persistence.
- [ ] Program/routine mutation never rewrites completed history or an active immutable snapshot.
- [ ] Progress arrows and graphs compare genuinely comparable records and disclose metric/units.
- [ ] Actual domain limits replace prototype constants.
- [ ] Feature scope and permissions respected; no live alerts, generators or backups implied by mock toggles.
- [ ] Native screenshot comparison and owner approval completed before merge.
