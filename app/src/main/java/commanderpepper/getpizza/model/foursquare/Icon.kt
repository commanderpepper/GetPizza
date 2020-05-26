package commanderpepper.getpizza.model.foursquare

import com.squareup.moshi.Json

data class Icon (
    @Json(name = "prefix")
    val prefix : String,
    @Json(name = "suffix")
    val suffix : String
    )