package commanderpepper.getpizza.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import commanderpepper.getpizza.foursquaremodels.Location
import commanderpepper.getpizza.retrofit.FourSquareService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainMapViewModel(application: Application) : AndroidViewModel(application) {

    private val fourSquareService: FourSquareService = FourSquareService.create()

    val locationLiveData = MutableLiveData<String>()

    @ExperimentalCoroutinesApi
    val locations by lazy {
        return@lazy locationLiveData.value?.let { setLocations(it) }
    }

    fun setLocationLiveData(latlng: LatLng) {
        if (latlng.latitude + 0.0182 > locationLiveData.value?.split(",")?.first()?.toDouble() ?: 0.0) {
            Log.d("UVM", "The Camera is moving")
            locationLiveData.value = "${latlng.latitude},${latlng.longitude}"
        }
    }

    fun updateLocationLiveData(latlng: LatLng) {
        if (latlng.latitude + 0.0182 > locationLiveData.value?.split(",")?.first()?.toDouble() ?: 0.0) {
            Log.d("UVM", "The Camera is moving")
            locationLiveData.value = "${latlng.latitude},${latlng.longitude}"
            updateLocations()
        }
    }

    fun getLocationFromLiveData(): String {
        Log.d("MVM", locationLiveData.value ?: "Nothing")
        // Iceland's lat and long. If I'm in iceland, oh no, I guess. 64.128288, -21.827774.
        return locationLiveData.value ?: "64.12,-21.82"
    }

    /**
     * Used to set up the locations inside this view model. Should be called only once.
     */
    @ExperimentalCoroutinesApi
    fun setLocations(latLng: String): MutableLiveData<Set<Location>> {
        val set = mutableSetOf<Location>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val flow = fourSquareService.searchForPizzas(latLng, "4bf58dd8d48988d1ca941735")
                    .response.venues.asFlow()
                    .map {
                        Log.d("Gotown", it.toString())
                        it.location
                    }
                    .flowOn(Dispatchers.IO)
                    .catch {
                        Log.d("Motown", "Something went wrong")
                    }
                flow.toCollection(set)
            }
        }
        return MutableLiveData(set)
    }

    /**
     * Call to update locations. Used
     */
    private fun updateLocations() {
        viewModelScope.launch {
            val set = mutableSetOf<Location>()
            withContext(Dispatchers.Default) {
                val flow = fourSquareService.searchForPizzas(locationLiveData.value!!, "4bf58dd8d48988d1ca941735")
                    .response.venues.asFlow()
                    .map {
                        Log.d("Gotown", it.toString())
                        it.location
                    }
                    .flowOn(Dispatchers.IO)
                    .catch {
                        Log.d("Motown", "Something went wrong")
                    }
                flow.toCollection(set)
                locations!!.postValue(set)
            }
        }
    }
}
