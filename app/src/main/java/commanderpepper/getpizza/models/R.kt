package SearchResults

import com.squareup.moshi.Json

data class R(
        @Json(name = "res_id")
        val resId: Int
)