package commanderpepper.getpizza.model.foursquare

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
    ) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Location

        if (address != other.address) return false
        if (crossStreet != other.crossStreet) return false
        if (lat != other.lat) return false
        if (lng != other.lng) return false
        if (!labeledLatLngs.contentEquals(other.labeledLatLngs)) return false
        if (distance != other.distance) return false
        if (postalCode != other.postalCode) return false
        if (cc != other.cc) return false
        if (city != other.city) return false
        if (state != other.state) return false
        if (!formattedAddress.contentEquals(other.formattedAddress)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + crossStreet.hashCode()
        result = 31 * result + lat.hashCode()
        result = 31 * result + lng.hashCode()
        result = 31 * result + labeledLatLngs.contentHashCode()
        result = 31 * result + distance
        result = 31 * result + postalCode.hashCode()
        result = 31 * result + cc.hashCode()
        result = 31 * result + city.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + formattedAddress.contentHashCode()
        return result
    }
}