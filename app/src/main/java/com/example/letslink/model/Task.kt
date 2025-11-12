package com.example.letslink.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task (

    @PrimaryKey var taskId: String = "",
    var eventId: String = "",
    var taskName: String = "",
    var taskDescription: String = "",
    var taskDuration: String = "",
    var dueDate: String = "",
    var taskStatus: String = "pending",
    var isSynced : Boolean = false
)
