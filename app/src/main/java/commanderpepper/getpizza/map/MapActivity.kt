package commanderpepper.getpizza.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
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
import commanderpepper.getpizza.room.entity.PizzaFav
import commanderpepper.getpizza.ui.PizzaInfoWindowAdapter
import commanderpepper.getpizza.viewmodel.MainMapViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

/**
 * Default zoom when using the map
 */
private const val zoom = 15.0f

/**
 * Default transparency
 */
private const val defaultTransparency = .85F

/**
 * Max marker count
 */
private const val maxMarkerCount = 200

class MapActivity : AppCompatActivity(),
    OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener,
    GoogleMap.OnCameraIdleListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var drawer: DrawerLayout
    private lateinit var mainMapViewModel: MainMapViewModel
    private lateinit var userInitialLatLng: LatLng
    private lateinit var navView: NavigationView

    private var markerMap = mutableMapOf<String, Marker?>()

    private var pizzaMap = mutableMapOf<String, PizzaFav>()

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
        val pair = marker.tag as Pair<Boolean, PizzaFav>
        val text = pair.second.name + " " + pair.second.address
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
        val pair = marker.tag as Pair<Boolean, PizzaFav>
        val favoriteStatus = pair.first
        val pizzaFav = pair.second

        Log.d("InfoClick", favoriteStatus.toString())

        // Check if this is a favorite or not. If this is not a favorite, make it one.
        if (!favoriteStatus) {
            markerMap[pizzaFav.id]!!.remove()
            markerMap.remove(pizzaFav.id)
            mainMapViewModel.addPizza(pizzaFav.apply {
                favorite = 1
            })
        } else {
            markerMap[pizzaFav.id]!!.remove()
            markerMap.remove(pizzaFav.id)
            mainMapViewModel.addPizza(pizzaFav.apply {
                favorite = 0
            })
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
//        mainMapViewModel.updateLocationLiveData(newLocation)
//        mainMapViewModel.setLocationFlow(newLocation)
        mainMapViewModel.updateLocationLiveData(newLocation)
//        removeMarkers()
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

        mainMapViewModel.setLocationFlow(userInitialLatLng)

        //Populate the data from this flow into the map
        mainMapViewModel.flowOfPizzaFav.onEach { pizzaList ->
            val map = pizzaList.map { it.id to it }.toMap().toMutableMap()
            pizzaMap.putAll(map)
            makeMarkersFromPizzaFav()
        }.launchIn(lifecycleScope)

        //Whenever a new location is given, update the location.
        mainMapViewModel.locationFlow.onEach {
            mainMapViewModel.updateLocationLiveData(it)
        }.launchIn(lifecycleScope)
    }

    private fun makeMarkersFromPizzaFav() {
        pizzaMap.forEach { pizzafav ->
            if (markerMap[pizzafav.key] == null) {
                if (pizzafav.value.favorite == 0) {
                    addDefaultMarkerFromPizzaFav(pizzaFav = pizzafav.value)
                } else {
                    addFavoriteMarkerFromPizzaFav(pizzaFav = pizzafav.value)
                }
            }
        }
    }

    /**
     * Removes markes from the @markerMap
     */
    private fun removeMarkers() {
        //Remove some items not inside the map of venues from the map
        val iter = markerMap.iterator()

        if (markerMap.size >= maxMarkerCount) {
            var i = 0
            while (iter.hasNext() && i <= 50) {
                val entry = iter.next()
                if (pizzaMap.containsKey(entry.key)) {
                    Timber.d("Match found")
                    entry.value?.remove()
                    iter.remove()
                }
                i++
            }
        }
    }


    /**
     * Add a default marker to the map
     * Default markers red and are slightly transparent
     */
    private fun addDefaultMarkerFromPizzaFav(pizzaFav: PizzaFav) {
        markerMap[pizzaFav.id] = map.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        pizzaFav.lat,
                        pizzaFav.lng
                    )
                )
                .alpha(defaultTransparency)
                .title(pizzaFav.name)
                .snippet(pizzaFav.address)
        )
        markerMap[pizzaFav.id]!!.tag = Pair(pizzaFav.favorite == 1, pizzaFav)
    }

    /**
     * Add a favorite marker to the map
     * Fav markers are blue
     */
    private fun addFavoriteMarkerFromPizzaFav(pizzaFav: PizzaFav) {
        markerMap[pizzaFav.id] = map.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        pizzaFav.lat,
                        pizzaFav.lng
                    )
                )
                .title(pizzaFav.name)
                .snippet(pizzaFav.address)
                .icon(
                    BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
        )
        markerMap[pizzaFav.id]!!.tag = Pair(pizzaFav.favorite == 1, pizzaFav)
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
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
                    map.moveCamera(cameraUpdate)
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
