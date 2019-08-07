package commanderpepper.getpizza.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import commanderpepper.getpizza.repo.RetrofitRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainMapViewModel(application: Application) : AndroidViewModel(application) {

    private var retrofitRepo: RetrofitRepo = RetrofitRepo()

    private var markers: MutableLiveData<List<Marker>>? = null
    // Location of the user
    private var userLocation: MutableLiveData<LatLng>? = null
    // Location of the map
    private var mapLocation: MutableLiveData<LatLng>? = null

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

    @InternalCoroutinesApi
    fun setUpFlow() {
        val ll = "${userLocation!!.value!!.latitude},${userLocation!!.value!!.longitude}"
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                retrofitRepo.supplyFlow(ll)
                activateFlow()
            }
        }
    }

    @InternalCoroutinesApi
    fun activateFlow() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                retrofitRepo.activateFlow()
            }
        }
    }
}