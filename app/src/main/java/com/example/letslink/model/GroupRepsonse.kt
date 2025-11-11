package com.example.letslink.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class GroupResponse(
    val groupId: String = UUID.randomUUID().toString(),
    val userId: String = UUID.randomUUID().toString(),
    val groupName: String = "",
    val description: String ="",
    val inviteLink: String ="",
    val members: List<String> = emptyList()
)