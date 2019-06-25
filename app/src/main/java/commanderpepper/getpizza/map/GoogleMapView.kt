package commanderpepper.getpizza.map


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
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
    private var fab: FloatingActionButton? = null

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

        val observer = RestaurantMarkers(latitude, longitude, mapView)
        val dis = mapViewModel.getPizzaMapMarkers(latitude, longitude)
        dis.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)

        fab = rootView.findViewById(R.id.mapViewFAB) as FloatingActionButton
        fab!!.setOnClickListener {
            var fabLat: Double = latitude
            var fabLon: Double = longitude
            mapView.getMapAsync {
                it.clear()
                fabLat = it.cameraPosition.target.latitude
                fabLon = it.cameraPosition.target.longitude
            }
            val fabObserver = RestaurantMarkers(fabLat, fabLon, mapView)
            val fabObservable = mapViewModel.getPizzaMapMarkers(fabLat, fabLon)
            fabObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fabObserver)

        }

        Log.d("Observer", "${observer.isDisposed}")
        return rootView
    }

    override fun onStart() {
        super.onStart()
        setLattitude()
        setLongitude()
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map

        googleMap!!.setOnCameraIdleListener(this)
        googleMap!!.setOnCameraMoveStartedListener(this)
        googleMap!!.setOnCameraMoveListener(this)
        googleMap!!.setOnCameraMoveCanceledListener(this)
    }

    override fun onCameraMove() {
        Log.d("Camera Event", "On Camera Move")
        Log.d("Camera Event", googleMap!!.cameraPosition.target.toString())

        val cLat = googleMap!!.cameraPosition.target.latitude
        val cLon = googleMap!!.cameraPosition.target.longitude

        Log.d("Location", "$cLat")
        Log.d("Location", "$cLon")

        latitude = cLat
        longitude = cLon
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
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
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
                        ).zoom(10f).build()
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
                        ).zoom(10f).build()
                    )
                )
            }
        }

    }

}

