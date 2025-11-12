package com.example.letslink.local_database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.letslink.model.Task
@Dao
interface TaskDao {
    @Upsert
    fun addTaskLocally(task: Task)

    @Query("SELECT * FROM tasks WHERE eventId = :eventId")
    suspend fun getTasksForEvent(eventId: String):List<Task>
}