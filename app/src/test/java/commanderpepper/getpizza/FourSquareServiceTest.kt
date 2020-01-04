package commanderpepper.getpizza

import commanderpepper.getpizza.foursquaremodels.SearchResponse
import commanderpepper.getpizza.retrofit.FourSquareService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FourSquareServiceTest {
    private var client: FourSquareService? = null

    // I should think how this clien is created
    @Before
    fun init() {
        client = FourSquareService.create()
    }

    @Test
    fun searchTest() = runBlocking {
        var search: SearchResponse? = null
        withContext(Dispatchers.IO) {
            search = client!!.searchForPizzas("40.755657,-73.587624", "4bf58dd8d48988d1ca941735")
        }
        Assert.assertTrue(search != null)
    }

    @Test
    fun testFlow() = runBlocking {
        var search: SearchResponse? = null
        withContext(Dispatchers.IO) {
            search = client!!.searchForPizzas("40.755657,-73.587624", "4bf58dd8d48988d1ca941735")
        }
        val venueFlow = search!!.response.venues.asFlow().map {
            it.location.lat to it.location.lng
        }.collect {
            Assert.assertTrue(it != null)
        }
    }
}