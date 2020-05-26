package commanderpepper.getpizza.model.foursquare

import com.squareup.moshi.Json

data class Response(
    @Json(name = "venues")
    val venues: Array<Venue>,
    @Json(name = "categories")
    val categories: Array<Category>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Response

        if (!venues.contentEquals(other.venues)) return false
        if (categories != null) {
            if (other.categories == null) return false
            if (!categories.contentEquals(other.categories)) return false
        } else if (other.categories != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = venues.contentHashCode()
        result = 31 * result + (categories?.contentHashCode() ?: 0)
        return result
    }
}