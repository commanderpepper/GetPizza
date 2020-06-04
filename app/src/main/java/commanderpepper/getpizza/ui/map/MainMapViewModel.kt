package commanderpepper.getpizza.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import commanderpepper.getpizza.repository.PizzaRepository
import commanderpepper.getpizza.repository.threeMileDistanceThreshold
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs

class MainMapViewModel : ViewModel() {

    var hasLatestUserLocation = false
        private set

    private val repository = PizzaRepository.getInstance()

    //Public read only flow for the UI to consume.
    val repoPizzaFlow = repository.getPizzaShopFlow()

    // Location channel used to store the user's location.
    // NYC is offered as a default value
    private val locationChannel = ConflatedBroadcastChannel<LatLng>().also {
        it.offer(LatLng(40.730, -73.935))
    }

    fun updateLocation(latlng: LatLng) {
        viewModelScope.launch {
            locationChannel.offer(latlng)
            withContext(Dispatchers.Main){
                requestForMorePizzaShops(locationChannel.value)
            }
            userHasSuppliedLocation()
        }
    }

    private fun userHasSuppliedLocation() {
        hasLatestUserLocation = true
    }

    private suspend fun requestForMorePizzaShops(latlng: LatLng) {
        repository.requestForMorePizzaFavs(latlng)
    }

    fun requestForMorePizzaShops(){
        viewModelScope.launch {
            val latlng = locationChannel.value
            requestForMorePizzaShops(latlng)
        }
    }

    /**
     * Adds a pizza to the local repo
     */
    fun addPizza(pizzaFav: PizzaFav) {
        viewModelScope.launch {
            repository.addPizza(pizzaFav)
        }
    }

    /**
     * Used to check if the distance between two LatLng is greater than distance parameter
     */
    fun compareLatLng(
        latlng1: LatLng,
        latlng2: LatLng,
        distance: Double = threeMileDistanceThreshold
    ): Boolean {

        val lat1 = latlng1.latitude
        val lng1 = latlng1.longitude
        val lat2 = latlng2.latitude
        val lng2 = latlng2.longitude

        val latDif = abs(lat1 - lat2)
        val lngDif = abs(lng1 - lng2)

        Timber.d(latDif.toString())
        Timber.d(lngDif.toString())

        return latDif >= distance || lngDif >= distance
    }
}
