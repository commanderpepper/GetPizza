package commanderpepper.getpizza.model.foursquare

import com.squareup.moshi.Json

data class Venue(
    @Json(name = "id")
    val id : String,
    @Json(name = "name")
    val name : String,
    val location : Location
    )