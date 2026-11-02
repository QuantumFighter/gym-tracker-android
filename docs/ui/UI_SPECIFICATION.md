# UI specification

## Shared presentation

Phone content reference width: approximately 380 CSS px. Native Android must adapt to actual dp/sp, safe areas and font scaling. The phone border and preview selector are presentation chrome, not app features. Barlow regular/medium, larger readable headings (about 34 sp), main text about 17–20 sp, secondary labels about 14–17 sp. Final screen layouts take precedence over earlier narrower typography demonstrations.

Light palette: background `#E9EDF3`; secondary surfaces `#DCE2EC`; main text `#202438`; secondary text `#646A80`; primary blue `#3156F5`; completion green `#23834B`; small timer control black `#161616`. Corners generally 12–16 dp; about 26 dp horizontal content padding. Preserve contrasting dark equivalents, descriptive accessibility labels, logical focus, and 48 dp effective touch targets even where visible buttons are smaller. Test actual contrast and scaling; screenshot approval is not an accessibility certification.

Bottom navigation: Home, Programs, Progress with large (~30 dp) symbols and compact vertical spacing. Settings is accessed from Home. Account is not part of this implementation handover.

## 20 screen references

| Key | Screen | Approved content and interaction |
|---|---|---|
| home | Home | Minimal next-routine label, routine name, circular Play on its right (62 dp visual circle, ~33 dp icon), left-aligned exercise count/time estimate, Edit routine, Settings, compact bottom navigation. Active session offers Resume rather than new Start. |
| picker | Warm-up duration | Routine header, Warm-up heading, no Get Ready label. Muted 2×2 options: 5:00, 10:00, 15:00, 20:00; no leading zero. Equal-width right-aligned numeric areas align 5 with 5 in 15. Presets thin and nearly full content width. Blue Custom time (~30 dp visible height) hides when tapped and reveals entry. Preset selection starts countdown immediately; no extra Start action. |
| timer | Warm-up countdown | Large countdown; no Get Ready label. Equal-width blue symbol-only pause/play and green Work Out controls. Work Out ends warm-up and opens selection, not finalisation. At zero, green Work Out fills the app content area until tapped. Warm Down remains reachable before expiry. |
| selection | Exercise selection | Large routine name replaces Your workout / Choose an exercise labels. Progress count and time estimate, progress bar, exercise cards and Warm Down at bottom. Partial sets shown; completed strike-through or hidden by preference. Free ordering. Hold a card to enter multi-select with Remove/Superset; hold a grouped member to offer broken-link Unlink. |
| exercise | Record exercise | Name + Details, saved-set table, current-set editor with weight/reps numeric inputs and +/−, Complete Set, compact rest controls, Back to exercises and Complete exercise. No Warm Down here. Editing set N places its correction editor between earlier and later saved rows. After prescribed sets are recorded, hide current-entry editor and show only small circular + to explicitly add another. |
| details | Exercise Details | Current prescription, starting weight, rest and measurement convention. Previous sessions lists up to three dates, each a one-line date/weight/reps/direction indicator and individually expandable set table. Instructions & notes expandable; pencil appears inside expanded area; save/cancel notes inline. Link to exercise history and Auto progression; return to recording without losing draft/saved state. |
| warmdown | Warm-down | Routine header and Warm-down heading. No completed/partial summary or Optional timer label. Ten-minute countdown; −, black play/pause, + with one-minute adjustment. Separate Return to Workout and green Finish Workout. |
| summary | Completed workout | Read-only routine, date/time, duration/set count, completed/partial/skipped status and expandable exercise set tables. Home and History actions. |
| history | Workout history | Monthly grouped session cards with date/time, duration and set count. Each opens its own summary. This is a log, not the main Progress screen. Accessible from Progress. |
| progress | Progress | Exercise selector, time range, labelled trend graph and separate Workout history entry. Example graph compares heaviest working-set weight; real values must identify unit/context and never imply all improvement equals load. |
| programs | Programs | Current program, routine list, next-workout indication, edit, add routine, switch program, create and suggested-program entry points. Future functionality must not appear working when unavailable. |
| routine | Routine editor | Routine title/edit name, exercise cards, set count +/−, rep range, starting weight, reorder arrows, pencil settings, Add exercise, Save routine. |
| library | Exercise Library | Search, muscle/equipment filters, multi-select add and Create custom exercise. Catalogue and routine picker can share visual structure with context-appropriate actions. |
| custom | New exercise | Name, main muscle, equipment, recording type, conditional weight convention, optional notes, Save. Prescription belongs to routine settings. Reps/time options are conceptual; preserve release-gated exercise types including distance scope. |
| exercise-settings | Routine exercise settings | Sets +/−, minimum/maximum reps, optional starting weight +/− and unset action, program-default/custom rest, replace exercise, Auto progression link, Save. |
| settings | Settings | Completed visibility, exercise-order hints, estimates, sound/vibration rest alerts; preferences apply automatically. Exercise Library access. Preserve actual permission behaviour rather than fake alert availability. |
| program-edit | Create/edit program | Program name, ordered routines with arrows, Add routine, active-program choice, Save. Create uses empty values; no invented routines. |
| switch | Switch program | Saved program options, current indicator, explicit Use this program confirmation. Existing session and history must not be rewritten. |
| suggest | Suggested program | Goal, session count, experience, equipment and exclusions; editable draft preview. Mockup is conceptual, not a generator. Reconcile session-length input required by approved generation scope; experience/exclusions do not silently authorise broader adaptive algorithms. |
| progression | Auto progression | User-facing mode name Auto progression (renamed from Double); Manual option; rule explanation and editable increment. Suggestions require acceptance, never silently change load. Access from exercise settings and Details. |

## Recording and rest details

Corrections update stable set identity, preserve chronology and do not restart rest. Save errors retain previous committed values and attempted input. Set completion only acknowledges after durable commit. Additional sets never silently change the prescribed progression threshold.

Rest is inline only on the individual exercise screen: centred countdown/control group, −15 and +15 at the sides, small black rounded play/pause (~32 dp visible) with filled white icon (~18 dp). No expand icon or expanded rest screen from this control. Timers use real elapsed/deadline state and survive supported interruptions; web intervals are not a production design.

## Multi-select and supersets

Long-press selects a card, then taps select/deselect others; a normal tap opens exercise. Provide accessible alternative to a long-press gesture. Superset needs at least two members. Grouped exercise exposes broken-link Unlink; unlink removes that member from the group, not from the workout. If only one member remains, dissolve the group and make both ordinary exercises. Saved work must remain intact. Preserve round/rest context for already-recorded sets; regrouping must not relabel historical performance silently. Mixed grouped/ungrouped selection behaviour still needs explicit domain refinement.

Remove was demonstrated as current-session removal with confirmation and retained recorded work, not deletion of reusable definitions. Exact skipped/partial status and progress-denominator semantics are not settled by the demo and must be resolved in its issue.

## History indicators

Green up, red down, grey horizontal line communicate the chosen comparison; labels must be available to assistive technology. Prototype arrows use working weight only. Do not silently equate these with overall progress: rep changes, mixed weights, assisted exercises, warm-ups and non-comparable equipment require a specified comparison rule. Unknown/no comparison needs an explicit neutral unavailable state, not a fabricated flat trend. For variable weights/reps, summarise faithfully (range or mixed indicator) and expose exact sets on expansion.

## Data and state requirements

Show real data or honest empty/unavailable states. No invented starting weight, duration or past session. Notes belong to the exercise; define snapshot versus live note behaviour explicitly without mutating immutable historical results. Existing completed history remains read-only. Routine edits affect future sessions, not active snapshots. Do not auto-merge, auto-advance a program twice, or clear an active session before committed finalisation.
