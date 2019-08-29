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

    @Query("SELECT EXISTS(SELECT 1 from pizzafav WHERE id = :pizzaId)")
    suspend fun checkForPizzaFav(pizzaId: String): Int

    @Query("DELETE FROM pizzafav")
    fun clearTableForTesting()
}