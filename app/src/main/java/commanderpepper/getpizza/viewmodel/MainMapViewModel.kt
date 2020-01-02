package commanderpepper.getpizza.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import commanderpepper.getpizza.repository.PizzaRepository
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

class MainMapViewModel(application: Application) : AndroidViewModel(application) {

//    private val fourSquareService: FourSquareService = FourSquareService.create()
//    private val pizzaDatabase = PizzaDatabase.getInstance(application)

    private val repository = PizzaRepository.getInstance()

//    val latLngLiveData = MutableLiveData<LatLng>()

    var location = LatLng(0.0, 0.0)
        private set

    lateinit var locationFlow: Flow<LatLng>

    val flowOfPizzaFav = repository.getPizzas()

    init {
        locationFlow = flow {
            emit(location)
        }
    }

    fun setLocationFlow(latlng: LatLng) {
//        locationFlow = flow {
//            emit(location)
//        }
    }

    fun updateLocationLiveData(latlng: LatLng) {
        viewModelScope.launch {
            Timber.d("The camera moved")
            if (compareLatLng(latlng, location)) {
                location = latlng
                setLocationFlow(latlng)
                repository.getPizzas(latlng)
            }
        }
    }

    fun addPizza(pizzaFav: PizzaFav) {
        viewModelScope.launch {
            repository.addPizza(pizzaFav)
        }
    }

//    /**
//     * Used to set up the locations inside this view model. Should be called only once.
//     */
//    @ExperimentalCoroutinesApi
//    fun setLocations(latLng: LatLng): MutableLiveData<Map<String, Venue>> {
//        val set = mutableSetOf<Venue>()
////        val mapOfVenues = mutableMapOf<String, Venue>()
//        viewModelScope.launch {
//            withContext(Dispatchers.Default) {
//                val flow = fourSquareService.searchForPizzas(
//                    convertLatLngtoString(latLng)
//                    , "4bf58dd8d48988d1ca941735"
//                )
//                    .response.venues.asFlow()
//                    .map {
//                        Log.d("Gotown", it.toString())
//                        it
//                    }
//                    .flowOn(Dispatchers.IO)
//                    .catch {
//                        Log.d("Motown", "Something went wrong")
//                    }
//                flow.toCollection(set)
//            }
//        }
////        return MutableLiveData(set)
//        return MutableLiveData(set.map { it.id to it }.toMap())
//    }
//
//    /**
//     * Call to update locations. Used
//     */
//    @ExperimentalCoroutinesApi
//    private fun updateLocations() {
//        viewModelScope.launch {
//            val set = mutableSetOf<Venue>()
//            withContext(Dispatchers.Default) {
//                val flow = fourSquareService.searchForPizzas(
//                    convertLatLngtoString(latLngLiveData.value!!),
//                    "4bf58dd8d48988d1ca941735"
//                )
//                    .response.venues.asFlow()
//                    .map {
//                        Log.d("Gotown", it.toString())
//                        it
//                    }
//                    .flowOn(Dispatchers.IO)
//                    .catch {
//                        Log.d("Motown", "Something went wrong")
//                    }
//                flow.toCollection(set)
//                locations!!.postValue(set.map { it.id to it }.toMap())
//            }
//        }
//        Log.d("Venues", locations!!.value!!.toString())
//        Log.d("VenuesSize", locations!!.value!!.size.toString())
//    }

//    private fun convertLatLngtoString(latLng: LatLng): String {
//        return "${latLng.latitude},${latLng.longitude}"
//    }

//    fun addPizza(venue: Venue) {
//        runBlocking {
//            withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
//                pizzaDatabase.pizzaDao().addPizzaFav(
//                    PizzaFav(
//                        venue.id,
//                        venue.location.lat.toDouble(),
//                        venue.location.lng.toDouble(),
//                        venue.location.address,
//                        venue.name
//                    )
//                )
//            }
//        }
//    }

//    fun deletePizza(venue: Venue) {
//        runBlocking {
//            withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
//                pizzaDatabase.pizzaDao().deletePizzaFav(
//                    PizzaFav(
//                        venue.id,
//                        venue.location.lat.toDouble(),
//                        venue.location.lng.toDouble(),
//                        venue.location.address,
//                        venue.name
//                    )
//                )
//            }
//        }
//    }

//    fun checkForPizza(id: String): Boolean {
//        var boolean = false
//        runBlocking {
//            val result = withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
//                pizzaDatabase.pizzaDao().checkForPizzaFav(id)
//            }
//            boolean = result == 1
//        }
//        Log.d("MainCheck", boolean.toString())
//        return boolean
//    }

    /**
     * Distance is about a quarter of a mile
     */
    private fun compareLatLng(
        latlng1: LatLng,
        latlng2: LatLng,
        distance: Double = 0.001953125
    ): Boolean {
        val lat1 = latlng1.latitude
        val lng1 = latlng2.longitude
        val lat2 = latlng2.latitude
        val lng2 = latlng2.longitude

        val latDif = abs(lat1 - lat2)
        val lngDif = abs(lng1 - lng2)

        Log.d("Compare", latDif.toString())
        Log.d("Compare", lngDif.toString())

        return latDif >= distance || lngDif >= distance
    }
}
