package commanderpepper.getpizza

import commanderpepper.getpizza.repository.PizzaRepository
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepositoryTest {
    private lateinit var pizzaRepository: PizzaRepository

    @Before
    fun init() {
        val context =
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        PizzaRepository.initialize(context)
        pizzaRepository = PizzaRepository.getInstance()
    }

    @Test
    fun test_adding_a_pizza() = runBlocking {
        val testPizza = PizzaFav(
            "1",
            40.1728,
            -74.5896
        )
        pizzaRepository.addPizza(testPizza)
        val retrievedPizza = pizzaRepository.getPizzas().first().first()
        assertThat(testPizza, CoreMatchers.equalTo(retrievedPizza))
    }

    @Test
    fun test_pizza_flow() = runBlocking {
        val testPizza = PizzaFav(
            "1",
            40.1728,
            -74.5896
        )
        pizzaRepository.addPizza(testPizza)

        val list = pizzaRepository.getPizzas().first()

        assertTrue(list.isNotEmpty())
    }

    @Test
    fun test_location_list() = runBlocking {
        val lat = 20.0
        val distance = 0.001953125
        val upperBound = lat + distance
        val lowerBound = lat - distance
        val testPizza = PizzaFav(
            "1",
            lat,
            -lat
        )
        pizzaRepository.addPizza(testPizza)
        val list = pizzaRepository.getPizzaLocation(lowerBound, upperBound)
        println("$list")
        assertTrue(list.isNotEmpty())
    }

    @Test
    fun test_location_list_against_false_positive() = runBlocking {
        val lat = 20.0
        val distance = 0.001953125
        val upperBound = lat + distance
        val lowerBound = lat - distance
        val testPizza = PizzaFav(
            "6",
            100.0,
            -100.0
        )
        pizzaRepository.addPizza(testPizza)
        val list = pizzaRepository.getPizzaLocation(lowerBound, upperBound)
        println("$list")
        assertTrue(list.isEmpty())
    }

    @Test
    fun test_location_list_with_lat_and_lng() = runBlocking {
        val lat = 75.0
        val lng = -75.00
        val distance = 0.001953125
        val upperLatBound = lat + distance
        val lowerLatBound = lat - distance

        val upperLngBound = lng + distance
        val lowerLngBound = lng - distance

        val testPizza = PizzaFav(
            "55",
            lat,
            lng
        )
        pizzaRepository.addPizza(testPizza)
        val list = pizzaRepository.getPizzaLocationUsingLatAndLng(
            lowerLatBound,
            upperLatBound,
            lowerLngBound,
            upperLngBound
        )
        println("$list")
        assertTrue(list.isNotEmpty())
    }

    @Test
    fun test_exclusivity_of_location_retriever() = runBlocking {
        val lat = 75.0
        val lng = -75.00
        val distance = 0.001953125
        val upperLatBound = lat + distance
        val lowerLatBound = lat - distance

        val upperLngBound = lng + distance
        val lowerLngBound = lng - distance

        val testPizza = PizzaFav(
            "55",
            lat,
            lng
        )

        val testPizza3 = PizzaFav(
            "65",
            lat + .0005,
            lng + .0005
        )

        val testPizza2 = PizzaFav(
            "75",
            lng,
            lat
        )

        pizzaRepository.addPizza(testPizza)
        pizzaRepository.addPizza(testPizza2)
        pizzaRepository.addPizza(testPizza3)

        val list = pizzaRepository.getPizzaLocationUsingLatAndLng(
            lowerLatBound,
            upperLatBound,
            lowerLngBound,
            upperLngBound
        )
        println("$list")
        assertThat(2, CoreMatchers.equalTo(list.size))
    }

    @Test
    fun test_if_insertion_replaces_values() = runBlocking {
        val fav_pizza = PizzaFav(
            "88",
            100.0,
            100.0,
            "place",
            "name",
            1
        )

        val no_fav_pizza = PizzaFav(
            "88",
            100.0,
            100.0,
            "place",
            "name",
            0
        )

        pizzaRepository.addPizzaIfNoneExists(fav_pizza)
        pizzaRepository.addPizzaIfNoneExists(no_fav_pizza)

        val list = pizzaRepository.getFavorites()
        println("$list")
        assertThat(1, CoreMatchers.equalTo(list.size))

    }
}