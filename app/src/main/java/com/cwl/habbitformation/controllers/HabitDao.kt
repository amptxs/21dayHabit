package com.cwl.habbitformation.controllers

import androidx.room.*
import com.cwl.habbitformation.models.Habit
import java.util.*

@Dao
interface HabitDao {
    @Insert
    fun add(habit: HabitDaoEntity): Long

    @Query("UPDATE HabitDaoEntity SET label=:label, " +
            "description=:description, " +
            " created=:created, " +
            " lastUpdate=:lastUpdate, " +
            " duration=:duration, " +
            " notifyAt=:notifyAt, " +
            " progress=:progress " +
            "WHERE id = :id")
    fun update(id: Int,
               label: String,
               description: String,
               created: Long,
               lastUpdate: Long?,
               duration: Int,
               notifyAt: Long?,
               progress: Int)

    @Update
    fun update(habit: HabitDaoEntity)

    @Query("SELECT * FROM HabitDaoEntity")
    fun getAll(): List<HabitDaoEntity>

    @Query("SELECT * FROM HabitDaoEntity WHERE id = :habitId")
    fun getById(habitId: Int): HabitDaoEntity

    @Query("DELETE FROM HabitDaoEntity WHERE id = :habitId")
    fun removeAt(habitId: Int)


}

@Entity
data class HabitDaoEntity(
    @PrimaryKey (autoGenerate = true) var id: Int,
    var label: String,
    var description: String,
    var created: Long,
    var lastUpdate: Long?,
    var duration: Int,
    var notifyAt: Long?,
    var progress: Int
)
{
    constructor() : this(0,"label", "description", Calendar.getInstance().timeInMillis,
        null, 21, null, 0)

    @TypeConverter
    fun castToNormal(): Habit {
        var habit = Habit(label, description, created, lastUpdate ,duration, notifyAt)
        habit.Progress = progress
        habit.EntityId = id
        return habit
    }

}