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



}