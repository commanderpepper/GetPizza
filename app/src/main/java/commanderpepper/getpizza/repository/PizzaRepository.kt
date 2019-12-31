package commanderpepper.getpizza.repository

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import commanderpepper.getpizza.foursquaremodels.SearchResponse
import commanderpepper.getpizza.foursquaremodels.Venue
import commanderpepper.getpizza.retrofit.FourSquareService
import commanderpepper.getpizza.room.PizzaDatabase
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

class PizzaRepository private constructor(context: Context) {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val fourSquareService: FourSquareService = FourSquareService.create()
    private val pizzaDatabase = PizzaDatabase.getInstance(context)

    fun addPizza(pizzaFav: PizzaFav) {
        scope.launch {
            pizzaDatabase.pizzaDao().addPizzaFav(pizzaFav)
        }
    }

    suspend fun getPizzas(latLng: LatLng) {
        val searchResponse = fourSquareService.searchForPizzas(
            latLng.concatString(),
            "4bf58dd8d48988d1ca941735"
        )
        val locations = searchResponse.response.venues.map {
            it.getPizza()
        }
        locations.forEach {
            addPizza(it)
        }
    }

    fun getMorePizzas(latLng: LatLng) {
        scope.launch {
            val searchResponse = fourSquareService.searchForPizzas(
                latLng.concatString(),
                "4bf58dd8d48988d1ca941735"
            )
            val locations = searchResponse.response.venues.map {
                it.getPizza()
            }
            locations.forEach {
                addPizza(it)
            }
        }
    }

    fun LatLng.concatString(): String {
        return "${this.latitude},${this.longitude}"
    }

    private fun getResultFromNetwork(ll: String) {
        scope.launch {
            fourSquareService.searchForPizzas(ll, "4bf58dd8d48988d1ca941735")
        }
    }

    private fun saveResultToDatabase(searchResponse: SearchResponse) {
        val locations = searchResponse.response.venues.map {
            it.getPizza()
        }
        locations.forEach {
            addPizza(it)
        }
    }

    fun getPizzas(): Flow<List<PizzaFav>> {
        return pizzaDatabase
            .pizzaDao()
            .getFlowOfFavorites()
            .distinctUntilChanged()
    }

    private fun Venue.getPizza(): PizzaFav {
        Timber.d(this.toString())
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

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = PizzaRepository(context)
            }
        }

        fun getInstance(): PizzaRepository {
            return INSTANCE ?: throw IllegalArgumentException("CrimeRepository must be initialized")
        }
    }
}