package commanderpepper.getpizza.repo

import android.util.Log
import commanderpepper.getpizza.foursquaremodels.Location
import commanderpepper.getpizza.foursquaremodels.Response
import commanderpepper.getpizza.foursquaremodels.SearchResponse
import commanderpepper.getpizza.retrofit.FourSquareService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class RetrofitRepo {

    private var fourSquareService: FourSquareService = FourSquareService.create()
    private lateinit var responseFlow: Flow<SearchResponse>

    /**
     * Return a flow containing locations
     */
    @ExperimentalCoroutinesApi
    suspend fun getPizza(latLon: String): Flow<Location>? {
        responseFlow = flow {
            fourSquareService.searchForPizzas(latLon, "4bf58dd8d48988d1ca941735").response
        }
        responseFlow.flowOn(Dispatchers.IO)
//            .single()

        Log.d("Golf", "Flowing in Repo")

        var locationFlow: Flow<Location>? = null
        responseFlow.collect { searchResponse ->
            locationFlow = searchResponse.response.venues
                .asFlow()
                .map { it.location }
                .flowOn(Dispatchers.Default)
        }

        return locationFlow
    }

    /**
     * Get a list of locations
     */
    @ExperimentalCoroutinesApi
    suspend fun getLocations(latLon: String): List<Location> {
        return fourSquareService.searchForPizzas(latLon, "4bf58dd8d48988d1ca941735").response.venues
            .asFlow()
            .map { it.location }
            .flowOn(Dispatchers.IO)
            .toList()
    }
}