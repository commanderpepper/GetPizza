package commanderpepper.getpizza.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import commanderpepper.getpizza.foursquaremodels.Location
import commanderpepper.getpizza.repo.RetrofitRepo
import kotlinx.coroutines.*

class MainMapViewModel(application: Application) : AndroidViewModel(application) {

    private var retrofitRepo: RetrofitRepo = RetrofitRepo()

    private var markers: MutableLiveData<List<Marker>>? = null
    // Location of the user
    private var userLocation: MutableLiveData<LatLng>? = null
    // Location of the map
    private var mapLocation: MutableLiveData<LatLng>? = null

    // List of locations
    var locations: MutableLiveData<List<Location>>? = null

    init {
        locations = MutableLiveData()
        locations!!.value = emptyList()
    }

    fun setUserLocation(latLng: LatLng) {
        if (userLocation == null) {
            userLocation = MutableLiveData()
            userLocation!!.value = latLng
        }
    }

    fun getUserLocation() = userLocation!!.value

    fun updateUserLocation(latLng: LatLng) {
        userLocation!!.value = latLng
    }

    fun setMapLocation(latLng: LatLng) {
        if (mapLocation == null) {
            mapLocation = MutableLiveData()
            mapLocation!!.value = latLng
        } else {
            mapLocation!!.value = latLng
        }
    }

    fun getMapLocation() = mapLocation!!.value

    @ExperimentalCoroutinesApi
    fun getLocations(latLng: String) {
        viewModelScope.launch {
            locations!!.value = retrofitRepo.getLocations(latLng)
        }
    }
}