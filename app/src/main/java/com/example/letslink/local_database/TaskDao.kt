package com.example.letslink.local_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.letslink.model.Task
@Dao
interface TaskDao {
    @Insert
    suspend fun addTaskLocally(task: Task)

    @Query("SELECT * FROM tasks WHERE eventId = :eventId")
    suspend fun getTasksForEvent(eventId: String):List<Task>

    @Query("SELECT * FROM tasks WHERE isSynced == 0")
    suspend fun getTasksToSync(): List<Task>

    @Query("UPDATE tasks SET isSynced = 'true' WHERE taskId = :taskId")
    suspend fun updateTaskSyncStatus(taskId: String)
}