package commanderpepper.getpizza.map

import android.Manifest
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
import com.google.android.material.navigation.NavigationView
import commanderpepper.getpizza.R
import commanderpepper.getpizza.databinding.ActivityMapBinding
import commanderpepper.getpizza.viewmodel.MainMapViewModel
import kotlinx.coroutines.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
    GoogleMap.OnCameraMoveListener {


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_map)

        val binding: ActivityMapBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()

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

        map.setOnCameraMoveListener(this)
//        setUpViewModel()
//        getCurrentMapLocation()

    }


    private fun getMapLocation(): String {
        return "${map.cameraPosition.target.latitude},${map.cameraPosition.target.longitude}"
    }

    // Called whenever the user ends a camera movement
    @InternalCoroutinesApi
    override fun onCameraMove() {
        setUpViewModel()
    }

    /**
     * Makes the view model
     */
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun setUpViewModel() {

        val mapActivity = this

        mainMapViewModel = ViewModelProviders.of(mapActivity).get(MainMapViewModel::class.java)
        Log.d("MapViewModel", mainMapViewModel.toString())

        //TODO Make it so that the location is properly set. It might have to be an async call.
        mainMapViewModel.mapLocation = getMapLocation()

        mainMapViewModel.locations.observe(
            mapActivity,
            Observer { set ->
                set.onEach {
                    Log.d("HUMZA", it.toString())
                }
            }
        )
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
        map.setOnMyLocationButtonClickListener {
            //TODO I think that this should call a different method that will call the view model
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
     * Gets current location
     */
    @InternalCoroutinesApi
    private fun getCurrentLocation() {
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
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.moveCamera(cameraUpdate)
//                    mainMapViewModel.setUpFlow()
//                    mainMapViewModel.activateFlow()
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
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
