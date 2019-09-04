package commanderpepper.getpizza.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
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
    GoogleMap.OnCameraIdleListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Longitude and Latitude of New York City
     */
    private var latitude: Double = 40.7128
    private var longitude: Double = -74.0060

    /**
     * Default zoom when using the map
     */
    private val zoom = 15.0f

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mainMapViewModel: MainMapViewModel
    private lateinit var userInitialLatLng: LatLng
    private lateinit var navView: NavigationView

    private var markerMap = mutableMapOf<String, Marker?>()

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

        drawer = binding.MainActivityDrawerLayout
        navView = binding.mainNavView
        navView.setNavigationItemSelectedListener(this)

    }

    /**
     * Handles user events in the drawer layout
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("DrawNav", item.toString())
        when (item.itemId) {
            R.id.favorites -> {
                Log.d("DrawNav", "Clicked on fav")
                val intent = Intent(this, FavoritesActivity::class.java)
                startActivity(intent)
            }
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
        map.setOnInfoWindowLongClickListener {
            handleLongInfoWindowClick(it)
        }
        Log.d("MapReady", "Map is ready")

        getCurrentLocation()
        setupMapListeners()

        map.setOnCameraIdleListener(this)
    }

    /**
     * When user long clicks on the info window it will go to a Google Search or a Web Search
     */
    private fun handleLongInfoWindowClick(marker: Marker) {
        val pair = marker.tag as Pair<Boolean, Venue>
        val text = pair.second.name + " " + pair.second.location.address
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_WEB_SEARCH

            putExtra(SearchManager.QUERY, text)
        }
        startActivity(sendIntent)
    }

    /**
     * When the user clicks on the info window it sets the pizza shop as favorite and saves that to the database.
     * Also can remove favorites.
     */
    private fun handleInfoWindowClick(marker: Marker) {
        val pair = marker.tag as Pair<Boolean, Venue>
        val boolean = pair.first

        Log.d("InfoClick", boolean.toString())

        if (boolean) {
            mainMapViewModel.deletePizza(pair.second)
            markerMap[pair.second.id]!!.remove()
            addDefaultMarker(pair.second)
        } else {
            mainMapViewModel.addPizza(pair.second)
            markerMap[pair.second.id]!!.remove()
            addFavMarker(pair.second)
        }
    }

    /**
     * Return the cameras current LatLng position
     */
    private fun getCameraLatLng(): LatLng {
        return LatLng(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude)
    }

    //Called when the camera is done moving. It will update the location within the viewmodel
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCameraIdle() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            updateViewModel(getCameraLatLng())
        }
    }

    /**
     * Called inside onCameraIdle
     */
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

    /**
     * Returns an observer that will update when the pizza shops list is updated
     * Updates the map with default and favorite locations
     * Also removes unnecessary locations
     */
    private fun addMarkersToMap(): Observer<Map<String, Venue>> {

        return Observer { venueMap ->
            Log.d("AddMarker", venueMap.toString())
            Log.d("VenueSize", venueMap.size.toString())
            //Add all items from the venues hash map to the google map
            venueMap.forEach { venue ->
                //Makes sure that a maker is only added once
                if (markerMap[venue.key] == null) {
                    if (mainMapViewModel.checkForPizza(venue.key)) {
                        addFavMarker(venue = venue.value)
                    } else {
                        addDefaultMarker(venue = venue.value)
                    }

                }
            }

            //Remove some items not inside the map of venues from the map
            val iter = markerMap.iterator()

            if (markerMap.size >= 200) {
                var i = 0
                while (iter.hasNext() && i <= 50) {
                    val entry = iter.next()
                    if (!venueMap.containsKey(entry.key)) {
                        entry.value?.remove()
                        iter.remove()
                    }
                    i++
                }
            }
        }
    }

    /**
     * Add a default marker to the map
     * Default markers red and are slightly transparent
     */
    fun addDefaultMarker(venue: Venue) {
        markerMap[venue.id] = map.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        venue.location.lat.toDouble(),
                        venue.location.lng.toDouble()
                    )
                )
                .alpha(.85F)
                .title(venue.name)
                .snippet(venue.location.address)
        )

        markerMap[venue.id]!!.tag =
            Pair(mainMapViewModel.checkForPizza(venue.id), venue)
    }

    /**
     * Add a favorite marker to the map
     * Fav markers are blue
     */
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
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
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

    /**
     * Callback for user response to the permission request
     */
    @InternalCoroutinesApi
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d("Permi", grantResults.toString())
        if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }
}
