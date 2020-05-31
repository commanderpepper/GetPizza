package commanderpepper.getpizza.ui.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import commanderpepper.getpizza.repository.PizzaRepository
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

class MainMapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PizzaRepository.getInstance()
    val pizzaFavFlow = repository.getPizzaShopFlow()

    val locationChannel = ConflatedBroadcastChannel<LatLng>().also {
        it.offer(LatLng(0.0, 0.0))
    }

    fun updateLocationLiveData(latlng: LatLng) {
        viewModelScope.launch {
            Timber.d("The camera moved")
            Timber.d("Parameter is $latlng")
            Timber.d("Channel is ${locationChannel.asFlow().first()}")

            if (locationChannel.asFlow().first() == LatLng(0.0, 0.0) || compareLatLng(
                    latlng,
                    locationChannel.asFlow().first()
                )
            ) {
//                repository.getPizzas(latlng)
                repository.requestForMorePizzaFavs(latlng)
                locationChannel.offer(latlng)
            }
        }
    }

    fun addPizza(pizzaFav: PizzaFav) {
        viewModelScope.launch {
            repository.addPizza(pizzaFav)
        }
    }

    suspend fun getPizzaUsingLocation(latLng: LatLng): List<PizzaFav> {
        return repository.getLocalPizzas(latLng)
    }

    /**
     * Used to check if the distance between two LatLng is greater than distance parameter
     * Distance is about a quarter of a mile
     */
    fun compareLatLng(
        latlng1: LatLng,
        latlng2: LatLng,
        distance: Double = 0.001953125
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
