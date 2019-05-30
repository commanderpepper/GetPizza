package SearchResults

import com.squareup.moshi.Json


data class Location(
        @Json(name = "address")
        val address: String,
        @Json(name = "city")
        val city: String,
        @Json(name = "city_id")
        val cityId: Int,
        @Json(name = "country_id")
        val countryId: Int,
        @Json(name = "latitude")
        val latitude: String,
        @Json(name = "locality")
        val locality: String,
        @Json(name = "locality_verbose")
        val localityVerbose: String,
        @Json(name = "longitude")
        val longitude: String,
        @Json(name = "zipcode")
        val zipcode: String
)