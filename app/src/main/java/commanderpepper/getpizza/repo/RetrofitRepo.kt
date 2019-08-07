package commanderpepper.getpizza.repo

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import commanderpepper.getpizza.retrofit.FourSquareService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class RetrofitRepo {

    private lateinit var flow: Flow<LatLng>


    suspend fun supplyFlow(ll: String) {
        flow = withContext(Dispatchers.IO) {
            FourSquareService.create().searchForPizzas(ll, "4bf58dd8d48988d1ca941735")
                .response.venues.asFlow()
                .map { LatLng(it.location.lat.toDouble(), it.location.lng.toDouble()) }
        }
    }

    @InternalCoroutinesApi
    suspend fun activateFlow() {
        flow.collect { Log.d("Flow", it.toString()) }
    }
}