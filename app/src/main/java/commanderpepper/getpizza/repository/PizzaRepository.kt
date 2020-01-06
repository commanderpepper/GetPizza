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
import timber.log.Timber

/**
 * Category ID for Pizza!
 */
private const val categoryId = "4bf58dd8d48988d1ca941735"

/**
 * About a mile in degrees if I did my math right
 */
private const val distanceThreshold = 0.018181818

/**
 * Cache limit, if the number of pizza shops is less than the cache limit within a certain area then ask the network for more.
 */
private const val cacheLimit = 25

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
     * Add a pizza to the database if that pizza fav isn't in the database
     */
    suspend fun addPizzaIfNoneExists(pizzaFav: PizzaFav) {
        scope.launch {
            pizzaDatabase.pizzaDao().addPizzaFavIfNoneExists(pizzaFav)
        }
    }

    /**
     * Get pizzas from the network and store those results
     */
    suspend fun getPizzas(latLng: LatLng) {

        Timber.d("Lat and Lng are $latLng")

        val lowerLatBound = latLng.latitude - distanceThreshold
        val upperLatBound = latLng.latitude + distanceThreshold
        val lowerLngBound = latLng.longitude - distanceThreshold
        val upperLngBound = latLng.longitude + distanceThreshold

        val pizzers = pizzaDatabase.pizzaDao().getPizzasNearLocationUsingLatAndLng(
            lowerLatBound,
            upperLatBound,
            lowerLngBound,
            upperLngBound
        )
        Timber.d("Pizzers' size ${pizzers.size}")
        Timber.d("Pizzers are $pizzers")

        if (pizzers.size < cacheLimit) {

            Timber.d("Calling the network")

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
     * Get all PizzaFavs with a favorite value of 1
     */
    suspend fun getFavorites() = pizzaDatabase.pizzaDao().getFavs()

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