# Verification and limitations

Packaged 2 November 2026. This is documentation/prototype verification, not Android implementation evidence.

## Executed successfully

- Confirmed 20 primary screen containers and selector routes.
- Node syntax check passed for all 24 inline script blocks in the standalone source.
- jsdom executed the source with no uncaught runtime errors during the exercised paths.
- Navigated all 20 selector routes.
- Exercised Home → warm-up preset → countdown → selection → Bench Press → save third set → correct first set → cancel correction → complete exercise → warm-down → finish → summary.
- Verified prescribed-count completion reveals extra-set +, correction exposes later sets below the editor, and saved sets reach the final summary.
- Exercised inline exercise note edit/save and verified displayed note text.
- Embedded Barlow font data and Lucide icons; removed remote font imports and unused external script tags from export. No user workout data or credentials included.
- Created the standalone export using the visualization export utility and supplied a host-independent rebuild script.

Raw DOM-check results: `verification-dom.json`. Syntax/DOM execution does not verify visual layout, accessibility, real browser rendering or Android behaviour.

## Could not execute here

Browser screenshot/layout checks were attempted with Playwright. The initial runtime lacked a browser. After a Chrome binary was obtained, launch failed with `socket() failed: Operation not permitted` in Chrome process-singleton initialisation. No PNGs or rendered-layout pass are claimed. `prototype/capture.cjs` is supplied for local capture, but has not completed successfully here. Follow `screenshots/README.md` and visually inspect the results.

No Android compilation, lint, Room test, migration, CI run or Galaxy A32 test was performed. GitHub content was read, not written.

## Prototype-specific limitations — do not implement accidentally

- State lives in JavaScript memory and resets on page reload. There is no durable saving, migration, encryption or process-death guarantee.
- Forms often navigate without persisting all configuration into the other screens. Catalogue creation, program generation, replacement, rename and several forms are navigation demonstrations only.
- History cards route to the shared sample summary, not a complete independent archive of sessions. Exercise Details tables and Progress graphs are synthetic and not recalculated from the recording store.
- Details heading can change with the selected exercise while its example prescription/history remain Bench Press fixtures. Do not use fixture values to infer other exercises' requirements.
- Session selection has its own in-memory list. Removal/grouping is not fully reconciled with the shared summary store; it does not prove data-safe mutation semantics. Screenshot and interaction approval do not justify deleting persisted sets.
- The initial active workout contains example pre-recorded sets; a real newly started workout must not.
- Timer quick mode is a preview control, not a production preference. The checkbox speeds the warm-up flow; warm-down retains its separate ten-minute demonstration timer. Navigation does not fully cancel/restore all timer state as native lifecycle code must.
- Some modal text explicitly identifies placeholders. Browser DOM checks do not establish transaction correctness, idempotency, real timer recovery or accessibility compliance.
- Runtime constraints and internal element IDs are inherited from independently built mockups. Refactor into proper Compose components; do not port this file as application architecture.

## Portability changes made for this handover

The approved screen layouts were retained. Font/icon dependencies were embedded for local use; standalone scaffolding was generated; a hidden-screen timer focus guard was added to avoid focus movement from an inactive demo view. These are export/runtime fixes, not new product decisions.

## Remaining visual work

Capture all 20 screens plus timer expiry, correction/extra-set, multi-select/link/unlink and expanded history/notes states. Review at narrow widths, larger text and dark mode. Create/resume/empty/storage-failure states require further explicit review before production completeness can be claimed.
