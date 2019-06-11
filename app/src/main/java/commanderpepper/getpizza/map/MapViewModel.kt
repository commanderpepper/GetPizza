package commanderpepper.getpizza.map

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import commanderpepper.getpizza.BaseViewModel
import commanderpepper.getpizza.models.Location
import commanderpepper.getpizza.models.SearchResults
import commanderpepper.getpizza.retrofit.ZomatoService
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class MapViewModel {

    val zomatoService by lazy {
        ZomatoService.create()
    }
    var observable: Observable<SearchResults>? = null

    fun getLatLngObservable(lat: Double, lng: Double): Observable<List<Pair<Double, Double>>> {
        return zomatoService.performSearch(lat, lng)
            .map { searchResults -> searchResults.restaurants.map { it.restaurant.location.latitude.toDouble() to it.restaurant.location.longitude.toDouble() } }
    }

    fun getRestaurants(lat: Double, lng: Double): Observable<List<Pair<String, Pair<Double, Double>>>>? {
        return zomatoService.performSearch(lat, lng)
            .map { searchResults -> searchResults.restaurants.map { it.restaurant.name to (it.restaurant.location.latitude.toDouble() to it.restaurant.location.longitude.toDouble()) } }
    }


}

