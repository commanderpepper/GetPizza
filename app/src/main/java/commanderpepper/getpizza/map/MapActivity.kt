package commanderpepper.getpizza.map

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import commanderpepper.getpizza.R
import commanderpepper.getpizza.retrofit.ZomatoService
import io.reactivex.disposables.Disposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class MapActivity : AppCompatActivity() {

    val zomatoService by lazy {
        ZomatoService.create()
    }

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val pizzaMap = GoogleMapView()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.map_container, pizzaMap)
        fragmentTransaction.commit()

//        performSearch()
    }

    private fun performSearch() {
        val latlon = mutableListOf<Pair<Double, Double>>()
        disposable = zomatoService.performSearch(40.76, -73.5)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
//            .map { searchResults -> searchResults.restaurants.map { it.restaurant.location.longitude to it.restaurant.location.latitude } }
            .subscribe { result -> Log.d("Humza", result.toString()) }
        disposable!!.dispose()
    }

}
