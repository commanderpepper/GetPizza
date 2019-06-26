package commanderpepper.getpizza.map


import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import commanderpepper.getpizza.R
import commanderpepper.getpizza.models.MapHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

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

        if (savedInstanceState == null) {
            setLattitude()
            setLongitude()
        } else {
            latitude = savedInstanceState.getDouble("lat")
            longitude = savedInstanceState.getDouble("lon")
        }

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

        getMarkersandPopulateMap(latitude, longitude, mapView)

        fab = rootView.findViewById(R.id.mapViewFAB) as FloatingActionButton
        fab!!.setOnClickListener {
            var fabLat: Double = latitude
            var fabLon: Double = longitude
            mapView.getMapAsync {
                it.clear()
                fabLat = it.cameraPosition.target.latitude
                fabLon = it.cameraPosition.target.longitude
            }
            getMarkersandPopulateMap(fabLat, fabLon, mapView)
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putDouble("lat", latitude)
        outState.putDouble("lon", longitude)
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

    fun getMarkersandPopulateMap(lat: Double, lon: Double, mapView: MapView) {
        val observer = RestaurantMarkers(lat, lon, mapView)
        val observable = mapViewModel.getPizzaMapMarkers(lat, lon)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
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

