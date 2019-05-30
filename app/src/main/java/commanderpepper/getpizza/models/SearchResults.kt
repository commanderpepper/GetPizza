package SearchResults

import com.squareup.moshi.Json

data class SearchResults(
        @Json(name = "restaurants")
        val restaurants: List<Restaurant>,
        @Json(name = "results_found")
        val resultsFound: Int,
        @Json(name = "results_shown")
        val resultsShown: Int,
        @Json(name = "results_start")
        val resultsStart: Int
)