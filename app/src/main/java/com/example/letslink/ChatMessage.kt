package com.example.letslink.models

data class ChatMessage(
    val isSent: Boolean,
    val text: String,
    val time: String,
    val senderID : String,
    val senderName : String
)


