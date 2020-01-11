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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPizzaFavIfNoneExists(pizzaFav: PizzaFav): Long

    @Delete
    suspend fun deletePizzaFav(pizzaFav: PizzaFav)

    @Query("SELECT EXISTS(SELECT 1 from pizzafav WHERE id = :pizzaId)")
    suspend fun checkForPizzaFav(pizzaId: String): Int

    @Query("SELECT * FROM pizzafav WHERE lat BETWEEN :lowerLatBound AND :upperLatBound")
    suspend fun getPizzasNearLocation(
        lowerLatBound: Double,
        upperLatBound: Double
    ): List<PizzaFav>

    @Query("SELECT * FROM pizzafav WHERE lat BETWEEN :lowerLatBound AND :upperLatBound AND lng BETWEEN :lowerLngBound AND :upperLngBound")
    suspend fun getPizzasNearLocationUsingLatAndLng(
        lowerLatBound: Double,
        upperLatBound: Double,
        lowerLngBound: Double,
        upperLngBound: Double
    ): List<PizzaFav>

    @Query("SELECT * FROM pizzafav WHERE lat BETWEEN :lowerLatBound AND :upperLatBound AND lng BETWEEN :lowerLngBound AND :upperLngBound")
    fun getFlowOfLocalPizzaShops(
        lowerLatBound: Double,
        upperLatBound: Double,
        lowerLngBound: Double,
        upperLngBound: Double
    ): Flow<List<PizzaFav>>

    @VisibleForTesting
    @Query("DELETE FROM pizzafav")
    fun clearTableForTesting()
}