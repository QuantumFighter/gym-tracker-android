# Module GymTracker

API reference for the Android application: a single-activity Kotlin application
using Compose, Material 3, Navigation 3 and lifecycle-aware ViewModel state.

`MainActivity` hosts `GymTrackerApp`, which owns the theme and navigation back stack.
`HomeRoute` collects the ViewModel's read-only state and passes it to `HomeScreen`.
Rendering receives immutable state and does not access storage.

B-02 adds an application-owned Room database, an exercise repository and a validated
pure Kotlin exercise model. Home prepares the catalogue through the repository and
shows a retry state when storage is unavailable. No workout policy is implemented.
B-03 adds transactional manual programs, independent reusable routines, ordered
memberships/occurrences/prescriptions and the persistent next-routine choice.
Internal feature implementation stays outside the public API reference.
Product decisions and development instructions remain in the repository's `docs` directory.

# Package com.quantumfighter.gymtracker

Android entry point. The activity owns platform lifecycle integration and hosts the
Compose application root.

# Package com.quantumfighter.gymtracker.app

Application composition root: theme, Home/Programs navigation and one shared
Room instance supplying exercise and program repositories.

# Package com.quantumfighter.gymtracker.feature.home

Home presentation boundary. The ViewModel exposes immutable state through a read-only
StateFlow; the route collects it with lifecycle awareness; the screen renders state.
Explicit route parameters and state-driven rendering provide injection seams for tests.

# Package com.quantumfighter.gymtracker.ui.theme

Shared Material 3 theme that follows the system's light or dark appearance.

# Package com.quantumfighter.gymtracker.data.exercise

Offline exercise catalogue repository; internal Room schema, DAO and original built-in
definitions. Atomic seeding retains stored identities and metadata. Recording access
rejects archived and unsupported measurement definitions.

# Package com.quantumfighter.gymtracker.domain

Android-independent exercise value model, required-metadata validation and explicit
measurement, load and ownership conventions.

# Package com.quantumfighter.gymtracker.data.program

Atomic manual-plan writes, ordered relational reads and the persistent Home cursor.
Internal Room entities separate reusable routines from program membership.

# Package com.quantumfighter.gymtracker.feature.programs

Lifecycle-aware plan lists, creation drafts and saved-routine details. ViewModels
orchestrate repository operations; Compose renders state and user actions.
