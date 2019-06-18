package commanderpepper.getpizza.map


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
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
    var lm: LocationManager? = null
    var location: Location? = null
    private val compositeDisposable = CompositeDisposable()
    private val mapViewModel = MapViewModel()

    private var latitude: Double = 40.7128
    private var longitude: Double = -74.0060

    //TODO ADD DATA BINDING BETWEEN VIEW and VIEWMODEL

//    init {
//        latitude = arguments!!.getDouble("latitude")
//        longitude = arguments!!.getDouble("longitude")
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        Log.d("Location", location.toString())
        Log.d("Location", lm.toString())

        setLattitude()
        setLongitude()

        Log.d("Location", latitude.toString())
        Log.d("Location", longitude.toString())

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

    fun setLongitude() {
        longitude = location!!.longitude
    }

    fun setLattitude() {
        latitude = location!!.latitude
    }

    @SuppressLint("MissingPermission")
    fun updateLocation() {
        location = lm!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
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

