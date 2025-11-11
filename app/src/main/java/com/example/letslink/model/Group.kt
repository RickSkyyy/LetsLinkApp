package com.example.letslink.model

import android.R
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "Groups",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("userId"),
            childColumns = arrayOf("userId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Group(
    @PrimaryKey
    var groupId:String = UUID.randomUUID().toString(),

    val userId: String,

    val groupName: String,

    val description: String,

    var inviteLink: String? = null,

)
{
    constructor() : this("","", "", "", null)
}