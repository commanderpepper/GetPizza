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
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class GoogleMapView : Fragment() {
    private var googleMap: GoogleMap? = null
    private lateinit var mapView: MapView
    private lateinit var viewmodel: MapViewModel
    val compositeDisposable = CompositeDisposable()
    val mapViewModel = MapViewModel()

    //TODO ADD DATA BINDING BETWEEN VIEW and VIEWMODEL

    init {
//        compositeDisposable.add(mapViewModel.getLatLngObservable(40.8426000000, -73.2883000000).subscribe())
//        compositeDisposable.dispose()
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

//        mapView.getMapAsync { mMap ->
//            googleMap = mMap
//
//            // For dropping a marker at a point on the Map
//            val sydney = LatLng(40.8426000000, -73.2883000000)
//            googleMap.addMarker(MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"))
//
//            // For zooming automatically to the location of the marker
//            val cameraPosition = CameraPosition.Builder().target(sydney).zoom(18f).build()
//            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//        }

//        compositeDisposable.addAll(mapViewModel.getLatLngObservable(40.76, -73.5).subscribe(LocationObserver(mapView,googleMap)))



        mapViewModel.getLatLngObservable(40.76, -73.5)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(LocationObserver(mapView))

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
//        compositeDisposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
//        viewmodel.onResume()
        mapView.onDestroy()
        compositeDisposable.dispose()
    }

    inner class LocationObserver(val mmapView: MapView) :
        Observer<List<Pair<Double, Double>>> {

        override fun onComplete() {
            print("hi")
        }

        override fun onSubscribe(d: Disposable) {
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
}

