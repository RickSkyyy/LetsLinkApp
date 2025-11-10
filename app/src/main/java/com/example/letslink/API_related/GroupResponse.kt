package com.example.api_test
import com.google.gson.annotations.SerializedName
import java.util.UUID

data class GroupResponse(
    val groupId: UUID,
    val userId: UUID,
    val groupName: String,
    val description: String,
    val inviteLink: String,
    val members: List<String>
)

