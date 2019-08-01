package commanderpepper.getpizza.foursquaremodels

import com.squareup.moshi.Json

data class LabeledLatLngs (
    @Json(name = "label")
    val label : String,
    
    val lat: Float,
    val lng: Float
)
