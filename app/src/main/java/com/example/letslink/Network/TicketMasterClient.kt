package com.example.letslink.Network
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.letslink.Network.TicektmasterApi

object TicketMasterClient {
    private const val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"

    val api : TicektmasterApi by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TicektmasterApi::class.java)
    }
}