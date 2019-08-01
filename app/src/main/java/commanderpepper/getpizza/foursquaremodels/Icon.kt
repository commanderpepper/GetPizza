package commanderpepper.getpizza.foursquaremodels

import com.squareup.moshi.Json

data class Icon (
    @Json(name = "prefix")
    val prefix : String,
    @Json(name = "suffix")
    val suffix : String
    )