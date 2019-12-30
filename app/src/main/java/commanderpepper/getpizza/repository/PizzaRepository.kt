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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class PizzaRepository private constructor(context: Context) {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val fourSquareService: FourSquareService = FourSquareService.create()
    private val pizzaDatabase = PizzaDatabase.getInstance(context)

    private fun addPizza(pizzaFav: PizzaFav) {
        scope.launch {
            pizzaDatabase.pizzaDao().addPizzaFav(pizzaFav)
        }
    }

    fun getMorePizzas(latLng: LatLng) {
        scope.launch {
            val searchResponse = fourSquareService.searchForPizzas(latLng.toString())
            val locations = searchResponse.response.venues.map {
                it.getPizza()
            }
            locations.forEach {
                addPizza(it)
            }
        }
    }

    fun LatLng.toString(): String {
        return "${this.latitude},${this.longitude}"
    }

    private fun getResultFromNetwork(ll: String) {
        scope.launch {
            fourSquareService.searchForPizzas(ll)
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
            .flowOn(Dispatchers.Default)
    }

    private fun Venue.getPizza(): PizzaFav {
        return PizzaFav(
            this.id,
            this.location.lat.toDouble(),
            this.location.lng.toDouble(),
            this.location.address,
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