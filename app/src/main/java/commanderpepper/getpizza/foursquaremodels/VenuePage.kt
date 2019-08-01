package commanderpepper.getpizza.foursquaremodels

import com.squareup.moshi.Json

data class VenuePage(
    @Json(name = "id")
    val id: String
)