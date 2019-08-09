package commanderpepper.getpizza.repo

import commanderpepper.getpizza.foursquaremodels.Location
import commanderpepper.getpizza.retrofit.FourSquareService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class RetrofitRepo {

    private var fourSquareService: FourSquareService = FourSquareService.create()

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