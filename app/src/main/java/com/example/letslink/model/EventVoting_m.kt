package com.example.letslink.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class EventVoting_m (
    val eventId : String = "",
    val title: String = "",
    val description : String = "",
    val groups : List<String> = emptyList(),
    val imageUrl : String = ""
) : Parcelable