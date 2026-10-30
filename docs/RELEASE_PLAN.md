# Release plan

The staged M1/M2/M3 scope was approved by the owner on 30 October 2026 (Q1). Sprint sequence remains proposed, not a fixed delivery-date commitment. Each sprint lasts two weeks. The Project uses provisional dates starting 2 November 2026; adjust them when development is authorised, rather than treating them as commitments. Capacity is unknown. Split/reforecast stories during refinement and extend the sequence if needed rather than weaken acceptance criteria. Implementation remains unauthorised.

## Milestones

Approved MVP distribution (Q8c): direct APK installation on the owner's Galaxy A32, with manual updates. Google Play publication is deferred to the later wish list. Plan stable application identity and signing-key custody for upgrade continuity; public repository visibility does not itself authorise publishing binaries.

| Gate | Exit evidence | Deliberately not yet included |
| --- | --- | --- |
| M0 Planning approved | Owner approves the consolidated planning foundation; implementation requires a separate explicit instruction | No code or repository exists in this deliverable |
| M1 Manual Workout Slice | Create manual routine, warm up, record out of order, warm down, finish, relaunch and inspect exact persisted history offline | Full superset/rest UX, generation, non-repetition entry and full release hardening |
| M2 Reliable Manual MVP | Complete manual program management, supersets, timers/recovery and screen-off rest alerts, minimal UI, estimates, progression, history, custom library, JSON/CSV and quality gates | Offline generation and duration/distance entry follow at M3 |
| M3 Full Initial Release | All confirmed requirements delivered, including generation and duration/distance; full regression and restore/migration evidence | Cloud, accounts, wearable integrations and advanced analytics |

M1 is internal-use validation; M2 is the first recommended personal-use release. No release with known silent data loss is acceptable. Features deferred from M2 to M3 remain committed product scope subject to approved sequencing.

## Sprint proposal

| Sprint | Stories / demonstrable objective | Acceptance at review |
| --- | --- | --- |
| 1 | B-01–B-05: complete one manual workout | On a clean device in airplane mode, create a routine, record two exercises out of order, warm down, finish, relaunch and read saved sets; retry-save test passes. M1 gate. Keep catalogue tiny and omit polish. |
| 2 | B-06–B-07: recover recording, deliver screen-off rest alerts and inspect storage safety | Kill process mid-session/rest and resume correct committed state; timer uncertainty is explicit after reboot; demonstrate screen-off alerts, permission-disabled fallback and alert rescheduling/cancellation; baseline migration fixtures and non-destructive error paths pass. |
| 3 | B-08–B-10: usable superset workout | Record a superset in different orders with one round rest; full-screen/compact timer agree; completed visibility persists; Home and remaining estimates reflect pending work. |
| 4 | B-11–B-13: reusable plans and explainable progression | Duplicate/archive/edit without changing history; add a custom substitute; one completed session with at least three prescribed working sets all reaching full reps produces an overridable increase; two-set prescriptions allow manual increases only, incomplete work does not count as failure. |
| 5 | B-14–B-15: portable complete data | Export/restore on a fresh installation; malformed import leaves data intact; CSV faithfully represents units and group context. |
| 6 | B-16–B-17: qualify manual MVP | All M2 scenarios pass; target-device accessibility/performance evidence and OS backup policy documented; release rehearsal completes. M2 gate. |
| 7 | B-18: editable offline generated program | Agreed rules handle valid/infeasible inputs, explain selections and produce an ordinary editable routine that can be recorded. |
| 8 | B-19–B-20: remaining exercise types and full release | Duration/distance recording works through history/backup/CSV; complete regression and fresh restore pass. M3 gate. |

Sprint 1 is a candidate high-value bundle; if setup consumes capacity, split it into two sprints while keeping M1 as the first functional milestone. Do not move sophisticated generation ahead of it. Each later story is independently reviewable even when grouped into a sprint.

## Dependency path

Approved Q6 scope keeps M3 generation limited to reviewed offline templates using training days, equipment, session length and goal. Advanced personalisation and adaptive programming are later wish-list items, with no delivery commitment.

Approved Q5b privacy scope: M2 exports are user-initiated, unencrypted and exclude all profile/account data. Password-protected backups belong on the later wish list; account/profile functionality remains outside the initial release. Any future profile/account export depends on encryption and separate approval. No exported data is intended to be publicly published.

Approval → toolchain/catalogue/manual routine → transactional session recording → recovery/rest/supersets → reusable plans/progression and portability → M2 qualification → generation/non-repetition support → M3 qualification.

Generation depends on reviewed catalogue metadata and usable manual models. Progression depends on trustworthy historical snapshots and group context. Import depends on a stable initial schema and migrations. Accessibility begins with the first controls; B-16 verifies the complete flow rather than introducing accessibility at the end.

## Risks and mitigation

| Risk | Response and owner | Checkpoint |
| --- | --- | --- |
| Scope exceeds solo capacity | Developer splits stories, uses WIP limit and reforecasts from completed work | Every sprint |
| Save/recovery loses or duplicates performance | Transaction boundaries, idempotency and real process-kill tests; developer | Sprints 1–2 and every persistence PR |
| Superset policy surprises user | Developer demonstrates the approved policy for free order, skips and unequal counts | Before B-08 |
| Reboot/clock change appears precise but wrong | Developer verifies approved alerts and visibly approximate/correctable reboot recovery | Before B-06 |
| Exercise/load histories are incomparable | Owner confirms equipment/load conventions; developer separates evidence contexts | Before B-13 |
| Import overwrites valuable records | Developer implements approved replacement safeguards and tests rollback before commit | Before B-14 |
| Built-in exercise content has unclear rights/quality | Use original/licensed reviewed metadata; retain attribution where required | B-02/B-12/B-18 |
| OS/version/CI constraints | Device/SDK spike, stable versions and available GitHub protections verified | B-01 |
| Poor gym usability | Short real-device recording demo with large controls and TalkBack/font checks | Each UI sprint |

All residual high-impact risks are reported at the release review. This planning pack proposes risk handling; it does not assert these risks have been tested away.
