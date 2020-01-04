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
        
        assertTrue(list.size > 0)
    }
}