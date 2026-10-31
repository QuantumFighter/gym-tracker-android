package com.quantumfighter.gymtracker.data.workout

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.quantumfighter.gymtracker.domain.Exercise

/** Source IDs are lineage, with no template foreign keys that could erase a snapshot. */
@Entity(tableName = "workout_sessions")
internal data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val programId: String?,
    val routineId: String?,
    val programName: String,
    val routineName: String,
    val startedAt: Long,
    val offsetSeconds: Int,
    val state: String = "WARM_UP",
    val warmupElapsedMs: Long = 0,
    val warmupAnchorMs: Long? = null,
    val restAnchorMs: Long? = null,
    val restDurationMs: Long = 0,
)

@Entity(tableName = "active_workout", foreignKeys = [ForeignKey(entity = WorkoutSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT)], indices = [Index(value = ["sessionId"], unique = true)])
internal data class ActiveWorkoutEntity(@PrimaryKey val id: Int = 1, val sessionId: String)

@Entity(tableName = "session_exercises", foreignKeys = [ForeignKey(entity = WorkoutSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT)], indices = [Index(value = ["sessionId", "ordinal"], unique = true)])
internal data class SessionExerciseEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val sourceOccurrenceId: String?,
    val ordinal: Int,
    @Embedded(prefix = "snapshot_") val definition: Exercise,
)

@Entity(tableName = "session_prescriptions", foreignKeys = [ForeignKey(entity = SessionExerciseEntity::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.RESTRICT)], indices = [Index(value = ["exerciseId", "ordinal"], unique = true)])
internal data class SessionPrescriptionEntity(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val sourcePrescriptionId: String?,
    val ordinal: Int,
    val repMin: Int,
    val repMax: Int,
    val loadGrams: Long?,
    val restSeconds: Int,
    val setType: String,
    val unit: String = "kg",
)

@Entity(tableName = "recorded_sets", foreignKeys = [ForeignKey(entity = SessionExerciseEntity::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.RESTRICT)], indices = [Index(value = ["exerciseId", "sequence"], unique = true), Index(value = ["commandId"], unique = true)])
internal data class RecordedSetEntity(
    @PrimaryKey val id: String,
    val commandId: String,
    val exerciseId: String,
    val sequence: Int,
    val recordingOrder: Long,
    val loadGrams: Long?,
    val repetitions: Int,
    val setType: String,
    val unit: String,
    val completedAt: Long,
    val offsetSeconds: Int,
)

internal data class SessionExerciseGraph(
    @Embedded val exercise: SessionExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "exerciseId") val prescriptions: List<SessionPrescriptionEntity>,
    @Relation(parentColumn = "id", entityColumn = "exerciseId") val sets: List<RecordedSetEntity>,
)

internal data class WorkoutGraph(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(entity = SessionExerciseEntity::class, parentColumn = "id", entityColumn = "sessionId") val exercises: List<SessionExerciseGraph>,
)
