package com.example.letslink

data class Message(
    val text: String,
    val isMine: Boolean // true if the message is from the user, false if from someone else
)