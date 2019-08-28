package commanderpepper.getpizza.room

import androidx.room.*
import commanderpepper.getpizza.room.entity.PizzaFav

@Dao
interface PizzaDAO {
    @Query("SELECT * from pizzafav")
    suspend fun getFavs(): List<PizzaFav>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPizzaFav(pizzaFav: PizzaFav): Long

    @Delete
    suspend fun deletePizzaFav(pizzaFav: PizzaFav)

//    @Query("SELECT id from pizzafav WHERE EXISTS(SELECT id FROM pizzafav WHERE id = :targetId)")
//    suspend fun doesPizzaExists(targetId: String)

    @Query("DELETE FROM pizzafav")
    fun clearTableForTesting()
}