package commanderpepper.getpizza.foursquaremodels

import com.squareup.moshi.Json

data class Location(
    @Json(name = "address")
    val address: String,
    @Json(name = "crossStreet")
    val crossStreet: String,
    @Json(name = "lat")
    val lat: Float,
    @Json(name = "lng")
    val lng: Float,
    @Json(name = "labeledLatLngs")
    val labeledLatLngs: Array<LabeledLatLngs>,
    @Json(name = "distance")
    val distance: Int,
    @Json(name = "postalCode")
    val postalCode: String,
    @Json(name = "cc")
    val cc: String,
    @Json(name = "city")
    val city: String,
    @Json(name = "state")
    val state : String,
    @Json(name = "formattedAddress")
    val formattedAddress : Array<String>
    )