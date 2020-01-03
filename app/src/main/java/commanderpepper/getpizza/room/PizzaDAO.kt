package commanderpepper.getpizza.room

import androidx.annotation.VisibleForTesting
import androidx.room.*
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.flow.Flow

//private const val distance: Double = 0.001953125

@Dao
interface PizzaDAO {

    @Query("SELECT * from pizzafav WHERE favorite == 1")
    suspend fun getFavs(): List<PizzaFav>

    @Query("SELECT * from pizzafav")
    fun getFlowOfFavorites(): Flow<List<PizzaFav>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPizzaFav(pizzaFav: PizzaFav): Long

    @Delete
    suspend fun deletePizzaFav(pizzaFav: PizzaFav)

    @Query("SELECT EXISTS(SELECT 1 from pizzafav WHERE id = :pizzaId)")
    suspend fun checkForPizzaFav(pizzaId: String): Int

    @Query("SELECT * FROM pizzafav WHERE lat BETWEEN (:lat + 0.001953125) AND (:lat - 0.001953125)")
    suspend fun getPizzasNearLocation(lat: Double): List<PizzaFav>

    @VisibleForTesting
    @Query("DELETE FROM pizzafav")
    fun clearTableForTesting()
}