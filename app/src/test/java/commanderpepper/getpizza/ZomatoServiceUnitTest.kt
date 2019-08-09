package commanderpepper.getpizza

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class ZomatoServiceUnitTest {
    var client: ZomatoService? = null

    @Before
    fun init() {
        client = ZomatoService.create()
    }

    @Test
    fun testSearchCall() {
        var clientResult: String? = null
        val searchresult = client!!.performSearch(40.76, -73.5)
            .subscribe { result -> clientResult = result.toString() }
//        Log.d("Humza", searchresult.toString())
        assertTrue(clientResult != null)
    }
}