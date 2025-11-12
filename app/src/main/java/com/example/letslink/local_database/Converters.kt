package com.example.letslink.local_database

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class Converters {
        @TypeConverter
        fun fromStringList(value: List<String>?): String {
            return Gson().toJson(value)
        }

        @TypeConverter
        fun toStringList(value: String): List<String> {
            val listType = object : TypeToken<List<String>>() {}.type
            return Gson().fromJson(value, listType)
        }

}