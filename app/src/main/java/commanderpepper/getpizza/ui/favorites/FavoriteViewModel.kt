package commanderpepper.getpizza.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import commanderpepper.getpizza.room.PizzaDatabase
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val pizzaDatabase = PizzaDatabase.getInstance(application)

    val favorites by lazy {
        return@lazy getFavs()
    }

    private fun getFavs(): MutableList<PizzaFav> {
        var list = mutableListOf<PizzaFav>()
        runBlocking {
            withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
                list = pizzaDatabase.pizzaDao().getFavs().toMutableList()
            }
        }
        return list
    }

}