package commanderpepper.getpizza.model.foursquare

import com.squareup.moshi.Json

data class Meta(
    @Json(name = "code")
    val code: Int,
    @Json(name = "requestId")
    val requestId: String
)