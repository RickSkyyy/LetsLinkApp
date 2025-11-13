package com.example.letslink.model



data class EventResponse(
    val _embedded : Embedded?
)
data class Embedded(
    val events : List<TMEvent>
)
data class TMEvent(
    val name : String,
    val dates : TMDateInfo,
    val url : String?,
    val images: List<TMImage>?,
    val _embedded : TMLocationWrapper?,
    val priceRanges: List<TMPriceRange>?
)
data class TMImage(
    val url: String?,
    val width : Int?,
    val height: Int?
)
data class TMDateInfo(
    val start: TMStartDate?
)
data class TMStartDate(
    val localDate : String?,
    val localTime : String?
)
data class TMLocationWrapper(
    val venues : List<TMVenue>?
)
data class TMVenue(
    val name : String?,
    val city : TMCity?,
    val country: TMCountry?
)
data class TMCity(
    val name : String?
)
data class TMCountry(
    val name : String?
)
data class TMPriceRange(
    val type: String?,
    val min: Double?,
    val max: Double?,
    val currency: String?
)
data class Start(
    val localDate : String
)