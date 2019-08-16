package commanderpepper.getpizza.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import commanderpepper.getpizza.R
import commanderpepper.getpizza.databinding.ActivityMapBinding
import commanderpepper.getpizza.viewmodel.MainMapViewModel
import kotlinx.coroutines.*


class MapActivity : AppCompatActivity(),
    OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraIdleListener {

    /**
     * Longitude and Latitude of New York City
     */
    private var latitude: Double = 40.7128
    private var longitude: Double = -74.0060

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mainMapViewModel: MainMapViewModel
    private lateinit var userInitialLatLng: LatLng

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_map)

        val binding: ActivityMapBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()

//        setUpViewModel()

        drawer = findViewById(R.id.MainActivityDrawerLayout)
//        askForPermission()
    }

    /**
     * Handles user events in the drawer layout
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favorites -> Log.d("Nav", "Clicked on fav")
        }
        return true
    }

    /**
     * Callback method called when the map is ready
     */
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        getCurrentLocation()
        setupMapListeners()

        map.setOnCameraIdleListener(this)
    }


    private fun getMapLocation(): String {
        return "${map.cameraPosition.target.latitude},${map.cameraPosition.target.longitude}"
    }

    fun getCameraLatLng(): LatLng {
        return LatLng(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude)
    }

    // Called whenever the user ends a camera movement
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCameraMove() {
        updateViewModel()
    }

    //Called when the camera is done moving
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCameraIdle() {
        updateViewModel(getCameraLatLng())
    }

    fun updateViewModel() {
//        mainMapViewModel.setLocationLiveData(userLatLng())
        mainMapViewModel.updateLocationLiveData(userLatLng())
        Log.d("UVM", "The Camera is done moving")
    }

    fun updateViewModel(cameraLocation: LatLng) {
        mainMapViewModel.updateLocationLiveData(cameraLocation)
    }

    /**
     * Makes the view model
     * Should be called when the user gets their location for the first time
     */
    @SuppressLint("MissingPermission")
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun setUpViewModel() {

        val mapActivity = this

        mainMapViewModel = ViewModelProviders.of(mapActivity).get(MainMapViewModel::class.java)
        Log.d("MapViewModel", mainMapViewModel.toString())

//        var location = LatLng(0.0, 0.0)
        mainMapViewModel.setLocationLiveData(userInitialLatLng)

        Log.d("MapVM", mainMapViewModel.getLocationFromLiveData())

        //
        mainMapViewModel.locations!!.observe(
            mapActivity,
            Observer { set ->
                Log.d("SetOfLocation", set.toString())
                set.forEach {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(it.lat.toDouble(), it.lng.toDouble()))
                            .title(it.state)
                    )
                }
            }
        )

        mainMapViewModel.locationLiveData.observe(mapActivity, Observer {
            mainMapViewModel.setLocations(it)
        })
    }

    @SuppressLint("MissingPermission")
    private fun userLatLng(): LatLng {
        var location1 = LatLng(0.0, 0.0)
        fusedLocationClient.lastLocation.addOnCompleteListener {
            location1 = LatLng(it.result!!.longitude, it.result!!.latitude)
        }
        return location1
    }

    /**
     * Calls stuff when the map moves
     */
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun setupMapListeners() {
        map.setOnCameraMoveListener {
            Log.d("MAP", "The map moved")
//            getCurrentMapLocation()
//            mainMapViewModel.getLocations(getMapLocation())
        }
        /**
         * Called when the user clicks on the my location button on the top right
         */
        map.setOnMyLocationButtonClickListener {
            /**
             * TODO I think that this should call a different method that will call the view model
             * I say this because the getCurrentLocation should be used once I think
             */

            getCurrentLocation()
            true
        }
    }

    /**
     * Set up a location client
     */
    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Gets current location, should only be used when the app is starting up
     */
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun getCurrentLocation() {
//        var latLng = LatLng(0.0, 0.0)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        } else {
            map.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    userInitialLatLng = latLng
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.moveCamera(cameraUpdate)
                    setUpViewModel()

                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
//        return latLng
    }

    /**
     * Request location permission
     */
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    override fun onStart() {
        super.onStart()
//        setUpGoogleMapView()
    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }
}
