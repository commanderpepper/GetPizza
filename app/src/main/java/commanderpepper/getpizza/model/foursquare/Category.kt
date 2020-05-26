package commanderpepper.getpizza.model.foursquare

import com.squareup.moshi.Json

data class Category(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "pluralName")
    val pluralName: String,
    @Json(name = "shortName")
    val shortName: String,
    @Json(name = "icon")
    val icon: Icon,
    @Json(name = "primary")
    val primary : Boolean,
    @Json(name = "venuePage")
    val venuePage : VenuePage
)
