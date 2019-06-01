package commanderpepper.getpizza.models

import com.squareup.moshi.Json

data class R(
        @Json(name = "res_id")
        val resId: Int
)