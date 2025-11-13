package com.example.letslink.Network

import com.example.letslink.model.EventResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query



interface TicektmasterApi {
    @GET("events.json")
    suspend fun getEvents(
        @Query("apikey") apiKey : String,
        @Query("countryCode") countryCode: String = "ZA",
        @Query("keyword") keyword : String? = null
    ): Response<EventResponse>
}