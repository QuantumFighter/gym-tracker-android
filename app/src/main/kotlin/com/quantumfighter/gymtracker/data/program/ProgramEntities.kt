package com.quantumfighter.gymtracker.data.program

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.quantumfighter.gymtracker.data.exercise.ExerciseEntity

@Entity(tableName = "programs")
internal data class ProgramEntity(@PrimaryKey val id: String, val name: String, val notes: String = "", val archived: Boolean = false, val origin: String = "MANUAL")

@Entity(tableName = "routines")
internal data class RoutineEntity(@PrimaryKey val id: String, val name: String, val notes: String = "")

@Entity(
    tableName = "program_routines",
    foreignKeys = [
        ForeignKey(entity = ProgramEntity::class, parentColumns = ["id"], childColumns = ["programId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = RoutineEntity::class, parentColumns = ["id"], childColumns = ["routineId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index(value = ["programId", "ordinal"], unique = true), Index("routineId")],
)
internal data class ProgramRoutineEntity(@PrimaryKey val id: String, val programId: String, val routineId: String, val ordinal: Int)

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(entity = RoutineEntity::class, parentColumns = ["id"], childColumns = ["routineId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = ExerciseEntity::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index(value = ["routineId", "ordinal"], unique = true), Index("exerciseId")],
)
internal data class RoutineExerciseEntity(@PrimaryKey val id: String, val routineId: String, val exerciseId: String, val ordinal: Int)

@Entity(
    tableName = "set_prescriptions",
    foreignKeys = [ForeignKey(entity = RoutineExerciseEntity::class, parentColumns = ["id"], childColumns = ["routineExerciseId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["routineExerciseId", "ordinal"], unique = true)],
)
internal data class SetPrescriptionEntity(
    @PrimaryKey val id: String,
    val routineExerciseId: String,
    val ordinal: Int,
    val repMin: Int,
    val repMax: Int,
    val loadGrams: Long?,
    val restSeconds: Int,
    val setType: String = "WORKING",
)

/** Only the B-03 cursor is introduced; other settings arrive with their owning stories. */
@Entity(
    tableName = "app_settings",
    foreignKeys = [ForeignKey(entity = ProgramRoutineEntity::class, parentColumns = ["id"], childColumns = ["nextProgramRoutineId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("nextProgramRoutineId")],
)
internal data class AppSettingsEntity(@PrimaryKey val id: Int = 1, val nextProgramRoutineId: String?)

internal data class ExerciseGraph(
    @Embedded val occurrence: RoutineExerciseEntity,
    @Relation(parentColumn = "exerciseId", entityColumn = "id") val exercise: ExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "routineExerciseId") val sets: List<SetPrescriptionEntity>,
)

internal data class RoutineGraph(
    @Embedded val routine: RoutineEntity,
    @Relation(entity = RoutineExerciseEntity::class, parentColumn = "id", entityColumn = "routineId") val exercises: List<ExerciseGraph>,
)

internal data class MembershipGraph(
    @Embedded val membership: ProgramRoutineEntity,
    @Relation(entity = RoutineEntity::class, parentColumn = "routineId", entityColumn = "id") val routine: RoutineGraph,
)

internal data class ProgramGraph(
    @Embedded val program: ProgramEntity,
    @Relation(entity = ProgramRoutineEntity::class, parentColumn = "id", entityColumn = "programId") val memberships: List<MembershipGraph>,
)
