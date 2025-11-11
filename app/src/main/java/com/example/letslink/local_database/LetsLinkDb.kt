package com.example.letslink.local_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.letslink.local_database.GroupDao
import com.example.letslink.local_database.UserDao
import kotlin.jvm.java
import com.example.letslink.model.User
import com.example.letslink.model.Group
import com.example.letslink.API_related.UUIDConverter
//set up db and the dao will be have methods to write to room  (Lackner, 2025b)
@Database(entities = [User::class,Group::class], version = 9, exportSchema = false)
@TypeConverters(UUIDConverter::class)
abstract class LetsLinkDB : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    companion object {
        @Volatile
        private var INSTANCE: LetsLinkDB? = null

        fun getDatabase(context: Context): LetsLinkDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LetsLinkDB::class.java,
                    "eddieDB.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}