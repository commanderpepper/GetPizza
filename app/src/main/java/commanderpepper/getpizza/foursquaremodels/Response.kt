package commanderpepper.getpizza.foursquaremodels

import com.squareup.moshi.Json

data class Response(
    @Json(name = "venues")
    val venues: Array<Venue>,
    @Json(name = "categories")
    val categories: Array<Category>?
)