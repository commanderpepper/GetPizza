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
import java.lang.Math.abs

class GoogleMapView : Fragment(),
    GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraMoveCanceledListener,
    GoogleMap.OnCameraIdleListener,
    OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var viewmodel: MapViewModel
    private var googleMap: GoogleMap? = null

    var lm: LocationManager? = null
    var location: Location? = null
    private val compositeDisposable = CompositeDisposable()
    private val mapViewModel = MapViewModel()

    private var latitude: Double = 40.7128
    private var longitude: Double = -74.0060

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

        mapView.getMapAsync(this)

        try {
            MapsInitializer.initialize(activity!!.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // This is the observer which will subscribe to the observable
        var observer = RestaurantMarkers(latitude, longitude, mapView)
        var dis = mapViewModel.getPizzaMapMarkers(latitude, longitude)
        dis.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)

//        compositeDisposable.add(observer)
        Log.d("Observer", "${observer.isDisposed}")
//        Log.d("Observer", "${observer.}")
        return rootView
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map

        googleMap!!.setOnCameraIdleListener(this)
        googleMap!!.setOnCameraMoveStartedListener(this)
        googleMap!!.setOnCameraMoveListener(this)
        googleMap!!.setOnCameraMoveCanceledListener(this)
    }

    override fun onCameraMove() {
        var cLat: Double = 0.0
        var cLon: Double = 0.0

        Log.d("Camera Event", "On Camera Move")
        Log.d("Camera Event", googleMap!!.cameraPosition.target.toString())

        //Should call something that updates the amp with new markers
        //Calculate the distance between current latitude and longitude , if more than a certain value then perform the request and update the map

//        compositeDisposable.

        cLat = googleMap!!.cameraPosition.target.latitude
        cLon = googleMap!!.cameraPosition.target.longitude

        Log.d("Location", "$cLat")
        Log.d("Location", "$cLon")
    }

    override fun onCameraMoveStarted(reason: Int) {
        Log.d("Camera Event", "On Camera Move Stated $reason")
    }

    override fun onCameraMoveCanceled() {
        Log.d("Camera Event", "On Camera Move Canceled")
    }

    override fun onCameraIdle() {
        Log.d("Camera Event", "On Camera Idle")
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

    fun addMarker(mapHelper: MapHelper) {
        mapView.getMapAsync { map ->
            map.addMarker(
                MarkerOptions().position(LatLng(mapHelper.latitude, mapHelper.longitude)).title(
                    mapHelper.name
                )
            )
        }
    }


    inner class RestaurantMarkers(val lat: Double, val lon: Double, val mapview: MapView) :
        DisposableObserver<MapHelper>() {
        override fun onComplete() {
            mapview.getMapAsync { map ->
                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().target(
                            LatLng(
                                lat, lon
                            )
                        ).zoom(12.5f).build()
                    )
                )
            }
        }

        override fun onNext(t: MapHelper) {
            addMarker(t)
        }

        override fun onError(e: Throwable) {
            mapview.getMapAsync { map ->
                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().target(
                            LatLng(
                                lat, lon
                            )
                        ).zoom(12.5f).build()
                    )
                )
            }
        }

    }

}

