package com.quantumfighter.gymtracker.data.exercise

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.quantumfighter.gymtracker.domain.Exercise
import com.quantumfighter.gymtracker.domain.ExerciseOrigin
import com.quantumfighter.gymtracker.domain.LoadConvention
import com.quantumfighter.gymtracker.domain.MeasurementKind

/** Single-table representation; embedded value fields keep B-02 free of unused relationship tables. */
@Entity(tableName = "exercises")
internal data class ExerciseEntity(
    @PrimaryKey val id: String,
    val origin: ExerciseOrigin,
    val catalogueVersion: Int?,
    val name: String,
    val description: String,
    val muscleGroups: List<String>,
    val equipment: List<String>,
    val movementPatterns: List<String>,
    val exerciseType: String,
    val measurementKind: MeasurementKind,
    val loadConvention: LoadConvention,
    val archived: Boolean,
) {
    fun toExercise() = Exercise(
        id, origin, catalogueVersion, name, description, muscleGroups, equipment,
        movementPatterns, exerciseType, measurementKind, loadConvention, archived,
    )
}

internal fun Exercise.toEntity() = ExerciseEntity(
    id, origin, catalogueVersion, name, description, muscleGroups, equipment,
    movementPatterns, exerciseType, measurementKind, loadConvention, archived,
)

/** Tags cannot contain '|'; enum names are the schema-v1 storage tokens. Unknown tokens fail reads. */
internal class ExerciseConverters {
    @TypeConverter fun tagsToText(value: List<String>): String = value.joinToString("|")

    @TypeConverter fun textToTags(value: String): List<String> = value.split('|')

    @TypeConverter fun originToText(value: ExerciseOrigin): String = value.name

    @TypeConverter fun textToOrigin(value: String): ExerciseOrigin = ExerciseOrigin.valueOf(value)

    @TypeConverter fun measurementToText(value: MeasurementKind): String = value.name

    @TypeConverter fun textToMeasurement(value: String): MeasurementKind = MeasurementKind.valueOf(value)

    @TypeConverter fun loadToText(value: LoadConvention): String = value.name

    @TypeConverter fun textToLoad(value: String): LoadConvention = LoadConvention.valueOf(value)
}
