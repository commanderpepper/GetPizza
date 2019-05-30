package SearchResults

import com.squareup.moshi.Json

data class UserRating(
        @Json(name = "aggregate_rating")
        val aggregateRating: String,
        @Json(name = "rating_color")
        val ratingColor: String,
        @Json(name = "rating_text")
        val ratingText: String,
        @Json(name = "votes")
        val votes: String
)