package com.quantumfighter.gymtracker.data.exercise

import com.quantumfighter.gymtracker.domain.Exercise
import com.quantumfighter.gymtracker.domain.ExerciseOrigin
import com.quantumfighter.gymtracker.domain.LoadConvention
import com.quantumfighter.gymtracker.domain.MeasurementKind

/** Original MIT-licensed catalogue v1; IDs and load semantics are documented in docs/EXERCISE_CATALOGUE.md. */
internal object BuiltInCatalogue {
    val exercises: List<Exercise> = listOf(
        exercise(
            "6ae90adc-270d-4af1-ad4e-451410de1001",
            "Barbell back squat",
            "A squat with a barbell supported across the upper back. Load includes the bar and all plates.",
            listOf("quadriceps", "glutes"),
            listOf("barbell", "rack"),
            "squat",
            LoadConvention.TOTAL,
        ),
        exercise(
            "6ae90adc-270d-4af1-ad4e-451410de1002",
            "Barbell bench press",
            "A horizontal press while lying on a flat bench. Load includes the bar and all plates.",
            listOf("chest", "triceps"),
            listOf("barbell", "bench"),
            "horizontal_push",
            LoadConvention.TOTAL,
        ),
        exercise(
            "6ae90adc-270d-4af1-ad4e-451410de1003",
            "Barbell Romanian deadlift",
            "A hip hinge holding a barbell in front of the legs. Load includes the bar and all plates.",
            listOf("hamstrings", "glutes"),
            listOf("barbell"),
            "hip_hinge",
            LoadConvention.TOTAL,
        ),
        exercise(
            "6ae90adc-270d-4af1-ad4e-451410de1004",
            "Dumbbell overhead press",
            "An overhead press using one dumbbell in each hand. Load is the mass of one dumbbell; one rep is both arms.",
            listOf("shoulders", "triceps"),
            listOf("dumbbells"),
            "vertical_push",
            LoadConvention.PER_HAND,
        ),
        exercise(
            "6ae90adc-270d-4af1-ad4e-451410de1005",
            "Push-up",
            "A floor-based horizontal press using bodyweight. No body mass or external load is entered.",
            listOf("chest", "triceps"),
            listOf("bodyweight"),
            "horizontal_push",
            LoadConvention.BODYWEIGHT,
        ),
        exercise(
            "6ae90adc-270d-4af1-ad4e-451410de1006",
            "Machine-assisted pull-up",
            "A vertical pull on a counterweight-assisted machine. Load is positive indicated assistance; more assistance is easier.",
            listOf("back", "biceps"),
            listOf("assisted_pull_up_machine"),
            "vertical_pull",
            LoadConvention.ASSISTANCE,
        ),
    )

    private fun exercise(
        id: String,
        name: String,
        description: String,
        muscles: List<String>,
        equipment: List<String>,
        movement: String,
        load: LoadConvention,
    ) = Exercise(
        id = id,
        origin = ExerciseOrigin.BUILT_IN,
        catalogueVersion = 1,
        name = name,
        description = description,
        muscleGroups = muscles,
        equipment = equipment,
        movementPatterns = listOf(movement),
        exerciseType = "strength",
        measurementKind = MeasurementKind.REPETITIONS,
        loadConvention = load,
    )
}
