package commanderpepper.getpizza.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MainMapViewModel(application: Application) : AndroidViewModel(application) {


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
}