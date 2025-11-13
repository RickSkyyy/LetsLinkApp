package com.example.letslink.local_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.letslink.model.Event
@Dao
interface EventDao {

    @Insert
    suspend fun addEventLocally (event: Event)

     @Query("SELECT * FROM events WHERE ownerId = :userId")
     suspend fun getEventsForCurrentUser(userId: String): List<Event>

     @Query("SELECT * FROM events WHERE isSynced == 0")
     suspend fun getEventsToSync(): List<Event>

     @Query("UPDATE events SET isSynced = 'true' WHERE eventId = :eventId")
     suspend fun updateEvents(eventId: String)



}