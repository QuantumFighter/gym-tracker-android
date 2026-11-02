package com.quantumfighter.gymtracker.data.settings

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import kotlinx.coroutines.flow.map

/** Independent of the plan cursor: navigation/finalisation cannot reset visibility. */
@Entity(tableName = "ui_preferences")
internal data class UiPreferencesEntity(@PrimaryKey val id: Int = 1, val hideCompleted: Boolean = false)

@Dao
internal interface UiPreferencesDao {
    @Query("SELECT * FROM ui_preferences WHERE id = 1")
    fun observe(): kotlinx.coroutines.flow.Flow<UiPreferencesEntity?>

    @Upsert
    suspend fun save(value: UiPreferencesEntity)
}

/** Room is the sole authority; successful writes return only after durable commit. */
internal open class UiPreferencesRepository(private val database: GymDatabase) {
    open fun observeHiddenCompleted() = database.uiPreferencesDao().observe().map { it?.hideCompleted ?: false }

    open suspend fun hideCompleted(value: Boolean) {
        database.uiPreferencesDao().save(UiPreferencesEntity(hideCompleted = value))
    }
}
