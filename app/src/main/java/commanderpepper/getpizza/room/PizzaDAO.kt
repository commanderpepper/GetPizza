package commanderpepper.getpizza.room

import androidx.annotation.VisibleForTesting
import androidx.room.*
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.flow.Flow

@Dao
interface PizzaDAO {
    @Query("SELECT * from pizzafav")
    suspend fun getFavs(): List<PizzaFav>

    @Query("SELECT * from pizzafav")
    fun getFlowOfFavorites() : Flow<List<PizzaFav>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPizzaFav(pizzaFav: PizzaFav): Long

    @Delete
    suspend fun deletePizzaFav(pizzaFav: PizzaFav)

    @Query("SELECT EXISTS(SELECT 1 from pizzafav WHERE id = :pizzaId)")
    suspend fun checkForPizzaFav(pizzaId: String): Int

    @VisibleForTesting
    @Query("DELETE FROM pizzafav")
    fun clearTableForTesting()
}