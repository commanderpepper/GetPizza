package commanderpepper.getpizza.ui.map

import android.Manifest
import android.app.Activity
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
import commanderpepper.getpizza.ui.favorites.FavoritesActivity
import commanderpepper.getpizza.room.entity.PizzaFav
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

/**
 * Default zoom when using the map
 */
private const val zoom = 15.0f

/**
 * Favorite zoom when going to a favorite pizza shop
 */
private const val favoriteZoom = 17.5f

/**
 * Default transparency
 */
private const val defaultTransparency = .85F

/**
 * Favorite request code when going from the favorites activity back the map activity
 */
private const val REQUEST_FAV = 0

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
        setUpViewModel()

        drawer = binding.MainActivityDrawerLayout
        navView = binding.mainNavView
        navView.setNavigationItemSelectedListener(this)
    }

    /**
     * Handles user events in the drawer layout
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Timber.d(item.toString())
        when (item.itemId) {
            R.id.favorites -> {
                Timber.d("Clicked on fav")
                val intent = Intent(this, FavoritesActivity::class.java)
                startActivityForResult(
                    intent,
                    REQUEST_FAV
                )
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
        map.setInfoWindowAdapter(
            PizzaInfoWindowAdapter(
                this
            )
        )
        map.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
        map.setOnInfoWindowLongClickListener {
            handleLongInfoWindowClick(it)
        }

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

        Timber.d(favoriteStatus.toString())

        // Check if this is a favorite or not. If this is not a favorite, make it one.
        if (!favoriteStatus) {
            markerMap[pizzaFav.id]!!.remove()
            markerMap.remove(pizzaFav.id)
            mainMapViewModel.addPizza(pizzaFav.apply {
                favorite = 1
            })
            addFavoriteMarkerFromPizzaFav(pizzaFav)
        } else {
            markerMap[pizzaFav.id]!!.remove()
            markerMap.remove(pizzaFav.id)
            mainMapViewModel.addPizza(pizzaFav.apply {
                favorite = 0
            })
            addDefaultMarkerFromPizzaFav(pizzaFav)
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
     */
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun setUpViewModel() {

        val mapActivity = this

        mainMapViewModel = ViewModelProviders.of(mapActivity).get(MainMapViewModel::class.java)

        /**
         * Called every time the location in the viewmodel is updated.
         * When the view model updates the location is not inside the activity, I guess this is a decent separation of concerns
         */
        mainMapViewModel.locationChannel.asFlow()
            .onEach { userLocation ->
                Timber.d("UserLocation B: $userLocation")

                val map = mainMapViewModel.getPizzaUsingLocation(userLocation)
                    .map { it.id to it }.toMap().toMutableMap()
                pizzaMap.clear()
                pizzaMap.putAll(map)
                makeMarkersFromPizzaFav()

            }.launchIn(lifecycleScope)
    }

    /**
     * Removes all the existing markers, clears the marker map
     * Then adds the pizza favs as makers and makes those markers on the map
     */
    private fun makeMarkersFromPizzaFav() {

        markerMap.forEach {
            it.value!!.remove()
        }

        markerMap.clear()

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
     * Calls stuff when the user interacts with the map features
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
     * Get the user's current location when pressing top right button
     * and update the location within the view model.
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
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
                        zoom
                    )
                    map.moveCamera(cameraUpdate)
                    // Update the view model
//                    updateViewModel(latLng)

                } else {
                    Timber.e("No location found")
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
     * Gets user initial location, should only be used when the app is starting up, inside onMapReady
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

                /**
                 * If the returned location is not null and the location in the view model is 0,
                 * then move the camera to the user's location
                 */
                Timber.d("Location channel value : ${mainMapViewModel.locationChannel.value}")

                if (mainMapViewModel.locationChannel.value == LatLng(
                        0.0,
                        0.0
                    ) && location != null
                ) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    userInitialLatLng = latLng
                    val cameraUpdate =
                        CameraUpdateFactory.newLatLngZoom(latLng,
                            zoom
                        )
                    map.moveCamera(cameraUpdate)
                    updateViewModel(latLng)
                } else {
                    Timber.e("No location found")
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
        Timber.d(grantResults.toString())
        if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }

    /**
     * If the user clicks on the location button in their favorite pizza locations,
     * go to the spot on the map
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == REQUEST_FAV) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // The user selected a favorite location.
                data?.let {
                    val lat = data.extras?.get("lat") as Double
                    val lng = data.extras?.get("lng") as Double
                    Timber.d("User location is $lat and $lng")
                    val latLng = LatLng(lat, lng)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
                        favoriteZoom
                    )
                    map.moveCamera(cameraUpdate)
                } ?: return

            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }
}
