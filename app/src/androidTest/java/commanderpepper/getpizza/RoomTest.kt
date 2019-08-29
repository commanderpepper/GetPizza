package commanderpepper.getpizza

import commanderpepper.getpizza.room.PizzaDatabase
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class RoomTest {
    lateinit var database: PizzaDatabase

    @Before
    fun init() {
        val context =
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        database = PizzaDatabase.getInstance(context)
    }

    @Test
    fun addPizza() = runBlocking {
        val pizzaFav = PizzaFav(
            "1",
            1.0,
            2.0,
            "Address"
        )

        withContext(Dispatchers.IO) {
            database.pizzaDao().addPizzaFav(pizzaFav)
        }

        val pizzaList = withContext(Dispatchers.IO) {
            database.pizzaDao().getFavs()
        }

        assertThat(pizzaList.first(), CoreMatchers.equalTo(pizzaFav))
    }

    @Test
    fun addThenDeletePizza() = runBlocking {
        val pizzaFav = PizzaFav(
            "2",
            1.0,
            2.0,
            "Address"
        )

        withContext(Dispatchers.IO) {
            database.pizzaDao().addPizzaFav(pizzaFav)
        }

        withContext(Dispatchers.IO) {
            database.pizzaDao().deletePizzaFav(pizzaFav)
        }

        val pizzaList = withContext(Dispatchers.IO) {
            database.pizzaDao().getFavs()
        }

        assertThat(pizzaList.size, CoreMatchers.equalTo(0))
    }

    @Test
    fun checkForPizza() = runBlocking {
        val pizzaFav = PizzaFav(
            "3",
            1.0,
            2.0,
            "Address"
        )

        withContext(Dispatchers.IO) {
            database.pizzaDao().addPizzaFav(pizzaFav)
        }

        val result = withContext(Dispatchers.IO) {
            database.pizzaDao().checkForPizzaFav(pizzaFav.id)
        }

        assertThat(result, CoreMatchers.equalTo(1))
    }

    @After
    fun cleanUp() {
        database.pizzaDao().clearTableForTesting()
    }
}