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

    var locationFlow: Flow<LatLng>

    val flowOfPizzaFav = repository.getPizzas()

    init {
        locationFlow = flow {
            emit(location)
        }
    }

    fun updateLocationLiveData(latlng: LatLng) {
        viewModelScope.launch {
            Timber.d("The camera moved")
            if (compareLatLng(latlng, location)) {
                location = latlng
                repository.getPizzas(latlng)
            }
        }
    }

    fun addPizza(pizzaFav: PizzaFav) {
        viewModelScope.launch {
            repository.addPizza(pizzaFav)
        }
    }

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
