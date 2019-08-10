package commanderpepper.getpizza.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import commanderpepper.getpizza.foursquaremodels.Location
import commanderpepper.getpizza.repo.RetrofitRepo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn

class MainMapViewModel(application: Application) : AndroidViewModel(application) {

    private var retrofitRepo: RetrofitRepo = RetrofitRepo()

    private var markers: MutableLiveData<List<Marker>>? = null
    // Location of the user
    private var userLocation: MutableLiveData<LatLng>? = null
    // Location of the map
    private var mapLocation: MutableLiveData<LatLng>? = null

    // List of locations
    var locations: MutableLiveData<MutableList<Location>>? = null

    var locationFlow: Flow<Location>? = null

    init {
        locations = MutableLiveData()
//        locations!!.value
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
            withContext(Dispatchers.Default) {
                if (locationFlow == null) {
                    locationFlow = retrofitRepo.getPizza(latLng)
                }
            }
            withContext(Dispatchers.Default) {
                locationFlow!!.flowOn(Dispatchers.Default).collect {
                    //                        locations!!.value!!.add(it)
                    Log.d("Golf", it.toString())
                }
            }
        }

//            retrofitRepo.getPizza(latLng)!!
//                .flowOn(Dispatchers.Default)
//                .collect {
//                }
//            locations!!.value = retrofitRepo.getLocations(latLng)
    }
}
