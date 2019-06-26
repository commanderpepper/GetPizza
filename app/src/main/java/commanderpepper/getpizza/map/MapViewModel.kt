package commanderpepper.getpizza.map

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import commanderpepper.getpizza.BaseViewModel
import commanderpepper.getpizza.models.Location
import commanderpepper.getpizza.models.MapHelper
import commanderpepper.getpizza.models.SearchResults
import commanderpepper.getpizza.retrofit.ZomatoService
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toObservable

class MapViewModel {

    val zomatoService by lazy {
        ZomatoService.create()
    }

    fun getPizzaMapMarkers(lat: Double, lng: Double): Observable<MapHelper> {
        return zomatoService.performSearch(lat, lng)
            .map { searchResults ->
                searchResults.restaurants.map {
                    Log.d("Rest", it.toString())
                    MapHelper(
                        it.restaurant.location.latitude.toDouble(),
                        it.restaurant.location.longitude.toDouble(),
                        it.restaurant.name
                    )
                }
            }.flatMapIterable { x -> x }
    }


}

