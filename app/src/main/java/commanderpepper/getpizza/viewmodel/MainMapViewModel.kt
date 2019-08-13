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
import commanderpepper.getpizza.retrofit.FourSquareService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn

class MainMapViewModel(application: Application) : AndroidViewModel(application) {


//    private var retrofitRepo: RetrofitRepo = RetrofitRepo()

    private val fourSquareService: FourSquareService = FourSquareService.create()

    // List of markers
    private var markers: MutableLiveData<List<Marker>>? = null

    // List of locations
    var locations: MutableLiveData<MutableList<Location>>? = null

   //Flow of locations  
    var locationFlow: Flow<Location>? = null

    init {
        locations = MutableLiveData()
//        locations!!.value
    }

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
