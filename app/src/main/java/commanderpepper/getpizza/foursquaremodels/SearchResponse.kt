package commanderpepper.getpizza.foursquaremodels

import com.squareup.moshi.Json

data class SearchResponse(
    @Json(name = "meta")
    val meta: Meta,
    @Json(name = "response")
    val response: Response

)