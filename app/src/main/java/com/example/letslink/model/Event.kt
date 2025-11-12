package com.example.letslink.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey
    var eventId : String = "",
    var ownerId : String = "",
    var title: String= "",
    var description: String= "",
    var location: String= "",
    var startTime: String= "",
    var endTime: String= "",
    var date: String= "",
    var groups: List<String>? = emptyList(),
    var isSynced : Boolean = false
)