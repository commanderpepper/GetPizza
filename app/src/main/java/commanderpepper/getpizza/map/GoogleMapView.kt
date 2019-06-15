package commanderpepper.getpizza.map


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.gms.maps.MapsInitializer

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.GoogleMap

import commanderpepper.getpizza.R
import commanderpepper.getpizza.models.MapHelper
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class GoogleMapView : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var viewmodel: MapViewModel
    val compositeDisposable = CompositeDisposable()
    val mapViewModel = MapViewModel()

    private var latitude: Double = 40.7128
    private var longitude: Double = -74.0060

    //TODO ADD DATA BINDING BETWEEN VIEW and VIEWMODEL

    init {
        latitude = arguments!!.getDouble("latitude")
        longitude = arguments!!.getDouble("longitude")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        viewmodel = MapViewModel()

        val rootView = inflater.inflate(R.layout.fragment_google_map_view, container, false)

        mapView = rootView.findViewById(R.id.pmap) as MapView
        mapView.onCreate(savedInstanceState)

        mapView.onResume()

        try {
            MapsInitializer.initialize(activity!!.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val observer = RestaurantInfoObserver(mapView)

        val dis = mapViewModel.getRestaurantInfo(latitude, longitude)
        dis.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)


        compositeDisposable.add(observer)

        return rootView
    }

    override fun onResume() {
        super.onResume()
//        viewmodel.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
//        viewmodel.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
//        viewmodel.onResume()
        mapView.onDestroy()
        compositeDisposable.dispose()
    }

    inner class LocationObserver(val mmapView: MapView) :
        DisposableObserver<List<Pair<Double, Double>>>() {

        override fun onComplete() {
            print("hi")
        }

        override fun onNext(t: List<Pair<Double, Double>>) {
            Log.d("Humza", t.toString())
            mmapView.getMapAsync { map ->
                //                googleMap = map
                t.forEach {
                    map.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                it.first,
                                it.second
                            )
                        ).title("Marker Title").snippet("Marker Description")
                    )
                }
                val cameraPosition =
                    CameraPosition.Builder().target(LatLng(t.first().first, t.first().second)).zoom(10f).build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }

        override fun onError(e: Throwable) {
            print("hi")
        }

    }

    inner class RestaurantObserver(val mapview: MapView) :
        DisposableObserver<List<Pair<String, Pair<Double, Double>>>>() {

        override fun onComplete() {
            print("hi")
        }

        override fun onNext(t: List<Pair<String, Pair<Double, Double>>>) {
            Log.d("Humza", t.toString())
            mapview.getMapAsync { map ->
                t.forEach { reta ->
                    map.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                reta.second.first,
                                reta.second.second
                            )
                        ).title(reta.first)
                    )
                }
                val cameraPosition =
                    CameraPosition.Builder().target(LatLng(t.first().second.first, t.first().second.second)).zoom(11f)
                        .build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }

        }

        override fun onError(e: Throwable) {
            print("hi")
        }

    }

    inner class RestaurantInfoObserver(val mapview: MapView) : DisposableObserver<List<MapHelper>>() {
        override fun onComplete() {
            print("hi")
        }

        override fun onNext(t: List<MapHelper>) {
            mapview.getMapAsync { map ->
                t.forEach { mapHelper ->
                    map.addMarker(
                        MarkerOptions().position(LatLng(mapHelper.latitude, mapHelper.longitude)).title(
                            mapHelper.name
                        )
                    )

                }
                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().target(
                            LatLng(
                                t.first().latitude,
                                t.first().longitude
                            )
                        ).zoom(11f).build()
                    )
                )
            }
        }

        override fun onError(e: Throwable) {
            print("hi")
        }

    }

}

