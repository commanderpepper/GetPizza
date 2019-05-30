package SearchResults

import com.squareup.moshi.Json

data class Restaurant(
        @Json(name = "restaurant")
        val restaurant: RestaurantX
)