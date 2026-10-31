# Module GymTracker

API reference for the B-01 Android foundation: a single-activity Kotlin application
using Compose, Material 3, Navigation 3 and lifecycle-aware ViewModel state.

`MainActivity` hosts `GymTrackerApp`, which owns the theme and navigation back stack.
`HomeRoute` collects the ViewModel's read-only state and passes it to `HomeScreen`.
Rendering receives immutable state and does not access storage.

The `data` and `domain` directories currently describe ownership boundaries only.
No repository, database schema, catalogue or workout policy is implemented in B-01.
Future authorised stories add their API contracts and KDoc alongside their code.
Product decisions and development instructions remain in the repository's `docs` directory.

# Package com.quantumfighter.gymtracker

Android entry point. The activity owns platform lifecycle integration and hosts the
Compose application root.

# Package com.quantumfighter.gymtracker.app

Application composition root: theme and navigation ownership. The foundation has a
single Home destination.

# Package com.quantumfighter.gymtracker.feature.home

Home presentation boundary. The ViewModel exposes immutable state through a read-only
StateFlow; the route collects it with lifecycle awareness; the screen renders state.
Explicit route parameters and state-driven rendering provide injection seams for tests.

# Package com.quantumfighter.gymtracker.ui.theme

Shared Material 3 theme that follows the system's light or dark appearance.
