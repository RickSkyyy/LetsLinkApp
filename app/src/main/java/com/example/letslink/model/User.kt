package com.example.letslink.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class User (
    @PrimaryKey
    var userId: String = UUID.randomUUID().toString(),
    var firstName: String = "",
    var password: String = "",
    var dateOfBirth: String = "",
    var email: String = "",
    var fcmToken: String? = "",
    var liveLocation: String? = ""
)