package commanderpepper.getpizza.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import commanderpepper.getpizza.R
import commanderpepper.getpizza.databinding.ActivityMapBinding
import commanderpepper.getpizza.foursquaremodels.Venue
import commanderpepper.getpizza.ui.PizzaInfoWindowAdapter
import commanderpepper.getpizza.viewmodel.MainMapViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi


class MapActivity : AppCompatActivity(),
    OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener,
    GoogleMap.OnCameraIdleListener {

    /**
     * Longitude and Latitude of New York City
     */
    private var latitude: Double = 40.7128
    private var longitude: Double = -74.0060

    private val zoom = 15.0f

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mainMapViewModel: MainMapViewModel
    private lateinit var userInitialLatLng: LatLng

    private var markerMap = mutableMapOf<String, Marker>()

    /**
     * Inflates the layout, fragment and sets up the location client.
     * This also sets up the ViewModel.
     */
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMapBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()

        drawer = findViewById(R.id.MainActivityDrawerLayout)
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
        map.setInfoWindowAdapter(PizzaInfoWindowAdapter(this))
        map.setOnInfoWindowClickListener {
            //            Log.d("HI", "Nothing")
            handleInfoWindowClick(it)
        }
        Log.d("MapReady", "Map is ready")

        getCurrentLocation()
        setupMapListeners()

        map.setOnCameraIdleListener(this)
    }

    private fun handleInfoWindowClick(marker: Marker) {
        val pair = marker.tag as Pair<Boolean, Venue>
        val boolean = pair.first

        if (boolean) {
            marker.remove()
            mainMapViewModel.deletePizza(pair.second)

            addDefaultMarker(pair.second)
        } else {
            marker.remove()
            mainMapViewModel.addPizza(pair.second)

            addFavMarker(pair.second)
        }

        Toast.makeText(this, marker.title, Toast.LENGTH_SHORT).show()
    }

    private fun getCameraLatLng(): LatLng {
        return LatLng(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude)
    }

    //Called when the camera is done moving. It will update the location within the viewmodel
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCameraIdle() {
        updateViewModel(getCameraLatLng())
    }

    private fun updateViewModel(newLocation: LatLng) {
        mainMapViewModel.updateLocationLiveData(newLocation)
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

        mainMapViewModel.latLngLiveData.observe(mapActivity, Observer {
            mainMapViewModel.setLocations(it)
        })

        if (mainMapViewModel.latLngLiveData.value == null) {

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(userInitialLatLng, zoom)
            map.moveCamera(cameraUpdate)
            mainMapViewModel.setLocationLiveData(userInitialLatLng)
        }

        //Whenever the set of locations changes, call the add markers function
        mainMapViewModel.locations?.observe(
            mapActivity,
            addMarkersToMap()
        )
    }

    private fun addMarkersToMap(): Observer<Map<String, Venue>> {
        return Observer { venueMap ->
            //Add all items from the venues hash map to the google map
            venueMap.forEach { venue ->
                //Makes sure that a maker is only added once
                if (markerMap[venue.key] == null) {
//                    markerMap[venue.key] = map.addMarker(
//                        MarkerOptions()
//                            .position(
//                                LatLng(
//                                    venue.value.location.lat.toDouble(),
//                                    venue.value.location.lng.toDouble()
//                                )
//                            )
//                            .title(venue.value.name)
//                            .snippet(venue.value.location.address)
//
//                    )
//                    //Add a boolean to a the marker, indicating a favorite
//                    markerMap[venue.key]!!.tag =
//                        Pair(venue.value, mainMapViewModel.checkForPizza(venue.value.id))
                    if (mainMapViewModel.checkForPizza(venue.key)) {
                        addFavMarker(venue = venue.value)
                    } else {
                        addDefaultMarker(venue = venue.value)
                    }

                }
            }

            //Remove any items not inside the map of venues from the map
            markerMap.forEach {
                if (!venueMap.containsKey(it.key)) {
                    it.value.remove()
                    Log.d("Removal", it.toString())
                }
            }
        }
    }

    fun addDefaultMarker(venue: Venue) {
        markerMap[venue.id] = map.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        venue.location.lat.toDouble(),
                        venue.location.lng.toDouble()
                    )
                )
                .title(venue.name)
                .snippet(venue.location.address)
        )

        markerMap[venue.id]!!.tag =
            Pair(mainMapViewModel.checkForPizza(venue.id), venue)
    }

    fun addFavMarker(venue: Venue) {
        markerMap[venue.id] = map.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        venue.location.lat.toDouble(),
                        venue.location.lng.toDouble()
                    )
                )
                .title(venue.name)
                .snippet(venue.location.address)
                .icon(
                    BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
        )

        markerMap[venue.id]!!.tag =
            Pair(mainMapViewModel.checkForPizza(venue.id), venue)
    }

    /**
     * Calls stuff when the map moves
     */
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun setupMapListeners() {
        /**
         * Called when the user clicks on the my location button on the top right
         */
        map.setOnMyLocationButtonClickListener {
            getUserLocation()
            true
        }
    }

    /**
     * Get the user's current location and update the location within the view model
     */
    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        } else {
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
                    map.moveCamera(cameraUpdate)
                    // Update the view model
                    updateViewModel(latLng)

                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    /**
     * Set up a location client
     */
    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Gets current location, should only be used when the app is starting up, inside onCreate()
     */
    @ExperimentalCoroutinesApi
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
                    userInitialLatLng = latLng
//                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
//                    map.moveCamera(cameraUpdate)
                    // Set up the view model for the first time
                    setUpViewModel()
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

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }
}
