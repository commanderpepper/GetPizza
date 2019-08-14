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
import kotlinx.coroutines.flow.*
import okhttp3.Dispatcher

class MainMapViewModel(application: Application) : AndroidViewModel(application) {

    private val fourSquareService: FourSquareService = FourSquareService.create()
    var mapLocation: String = ""

    @ExperimentalCoroutinesApi
    val locations by lazy {
        val location = setLocations(mapLocation)
        Log.d("Motown", mapLocation)
        return@lazy location
    }

    @ExperimentalCoroutinesApi
    fun setLocations(latLng: String): MutableLiveData<Set<Location>> {
        val set = mutableSetOf<Location>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val flow = fourSquareService.searchForPizzas(latLng, "4bf58dd8d48988d1ca941735")
                    .response.venues.asFlow()
                    .map {
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
}
