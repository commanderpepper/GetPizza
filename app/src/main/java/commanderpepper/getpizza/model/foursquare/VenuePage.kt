package commanderpepper.getpizza.model.foursquare

import com.squareup.moshi.Json

data class VenuePage(
    @Json(name = "id")
    val id: String
)