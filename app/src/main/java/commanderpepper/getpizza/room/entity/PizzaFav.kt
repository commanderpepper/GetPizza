package commanderpepper.getpizza.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pizzafav")
data class PizzaFav(
    @PrimaryKey
    val id: String,
    val lat: Double,
    val lng: Double,
    val address: String
)