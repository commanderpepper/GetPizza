package commanderpepper.getpizza.repository

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.android.gms.maps.model.LatLng
import commanderpepper.getpizza.foursquaremodels.Venue
import commanderpepper.getpizza.retrofit.FourSquareService
import commanderpepper.getpizza.room.PizzaDatabase
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Category ID for Pizza!
 */
private const val categoryId = "4bf58dd8d48988d1ca941735"

/**
 * This will get data from the UI to use, the UI should not be aware of the source of the data
 * This doesn't really work because PizzaFav does expose the Table schema but oh well
 * A private constructor prevents direct object creation
 */
class PizzaRepository private constructor(context: Context) {

    /**
     * Coroutine scope for the repository
     * The job is a SupervisorJob so that any cancellations in a child won't cancel a parent coroutine
     */
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val fourSquareService: FourSquareService = FourSquareService.create()
    private val pizzaDatabase = PizzaDatabase.getInstance(context)

    /**
     * Add a pizza to the database
     */
    suspend fun addPizza(pizzaFav: PizzaFav) {
        scope.launch {
            pizzaDatabase.pizzaDao().addPizzaFav(pizzaFav)
        }
    }

    /**
     * Get pizzas from the network and store those results
     */
    suspend fun getPizzas(latLng: LatLng) {

//        val pizzers = pizzaDatabase.pizzaDao().getPizzasNearLocation(latLng.latitude)
//        Timber.d("Pizzers are $pizzers")
        val searchResponse = fourSquareService.searchForPizzas(
            latLng.concatString(),
            categoryId
        )
        val locations = searchResponse.response.venues.map {
            it.getPizza()
        }
        locations.forEach {
            addPizza(it)
        }
    }

    /**
     * Take a LatLng object and return a string from Lat,Lng
     */
    fun LatLng.concatString(): String {
        return "${this.latitude},${this.longitude}"
    }

    /**
     * Get a Flow of List of PizzaFavs
     */
    fun getPizzas(): Flow<List<PizzaFav>> {
        return pizzaDatabase
            .pizzaDao()
            .getFlowOfFavorites()
//            .distinctUntilChanged()
    }

    @VisibleForTesting
    suspend fun getPizzaLocation(lowerBound: Double, upperBound: Double) =
        pizzaDatabase.pizzaDao().getPizzasNearLocation(lowerBound, upperBound)

    @VisibleForTesting
    suspend fun getPizzaLocationUsingLatAndLng(
        lowerLatBound: Double,
        upperLatBound: Double,
        lowerLngBound: Double,
        upperLngBound: Double
    ) =
        pizzaDatabase.pizzaDao().getPizzasNearLocationUsingLatAndLng(
            lowerLatBound,
            upperLatBound,
            lowerLngBound,
            upperLngBound
        )

    /**
     * Extension function to make a PizzaFav from a Venue object
     */
    private fun Venue.getPizza(): PizzaFav {
//        Timber.d(this.toString())
        return PizzaFav(
            this.id,
            this.location.lat.toDouble(),
            this.location.lng.toDouble(),
            if (this.location.address != null) this.location.address else "",
            this.name
        )
    }

    companion object {
        private var INSTANCE: PizzaRepository? = null

        /**
         * Initialize the repository, helps ensures a Singleton
         */
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = PizzaRepository(context)
            }
        }

        /**
         * Get an instance of the repository
         */
        fun getInstance(): PizzaRepository {
            return INSTANCE ?: throw IllegalArgumentException("CrimeRepository must be initialized")
        }
    }
}