package commanderpepper.getpizza.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import commanderpepper.getpizza.foursquaremodels.Venue
import commanderpepper.getpizza.retrofit.FourSquareService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class MainMapViewModel(application: Application) : AndroidViewModel(application) {

    private val fourSquareService: FourSquareService = FourSquareService.create()

    val latLngLiveData = MutableLiveData<LatLng>()

    @ExperimentalCoroutinesApi
    val locations by lazy {
        return@lazy latLngLiveData.value?.let { setLocations(it) }
    }

    fun setLocationLiveData(latlng: LatLng) {
        latLngLiveData.value = latlng
        setLocations(latlng)
        updateLocations()
        Log.d("Venues", latlng.toString())
        Log.d("Venues", latLngLiveData.value.toString())
    }

    fun updateLocationLiveData(latlng: LatLng) {
        if (compareLatLng(latlng, latLngLiveData.value ?: LatLng(0.0, 0.0))) {
            Log.d("UVM", "The camera moved")
            latLngLiveData.value = latlng
            updateLocations()
        }
    }

    /**
     * Used to set up the locations inside this view model. Should be called only once.
     */
    @ExperimentalCoroutinesApi
    fun setLocations(latLng: LatLng): MutableLiveData<Map<String, Venue>> {
        val set = mutableSetOf<Venue>()
//        val mapOfVenues = mutableMapOf<String, Venue>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val flow = fourSquareService.searchForPizzas(
                    convertLatLngtoString(latLng)
                    , "4bf58dd8d48988d1ca941735"
                )
                    .response.venues.asFlow()
                    .map {
                        Log.d("Gotown", it.toString())
                        it
                    }
                    .flowOn(Dispatchers.IO)
                    .catch {
                        Log.d("Motown", "Something went wrong")
                    }
                flow.toCollection(set)
            }
        }
//        return MutableLiveData(set)
        return MutableLiveData(set.map { it.id to it }.toMap())
    }

    /**
     * Call to update locations. Used
     */
    @ExperimentalCoroutinesApi
    private fun updateLocations() {
        viewModelScope.launch {
            val set = mutableSetOf<Venue>()
            withContext(Dispatchers.Default) {
                val flow = fourSquareService.searchForPizzas(
                    convertLatLngtoString(latLngLiveData.value!!),
                    "4bf58dd8d48988d1ca941735"
                )
                    .response.venues.asFlow()
                    .map {
                        Log.d("Gotown", it.toString())
                        it
                    }
                    .flowOn(Dispatchers.IO)
                    .catch {
                        Log.d("Motown", "Something went wrong")
                    }
                flow.toCollection(set)
                locations!!.postValue(set.map { it.id to it }.toMap())
            }
        }
        Log.d("Venues", locations!!.value!!.toString())
        Log.d("VenuesSize", locations!!.value!!.size.toString())
    }

    private fun convertLatLngtoString(latLng: LatLng): String {
        return "${latLng.latitude},${latLng.longitude}"
    }

    /**
     * Distance is about half a mile
     */
    fun compareLatLng(latlng1: LatLng, latlng2: LatLng, distance: Double = 0.003623188): Boolean {
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
