package commanderpepper.getpizza.ui.map

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import commanderpepper.getpizza.R
import commanderpepper.getpizza.databinding.ActivityMapBinding
import commanderpepper.getpizza.room.entity.PizzaFav
import commanderpepper.getpizza.ui.favorites.FavoritesActivity
import kotlinx.android.synthetic.main.activity_map.view.*
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

    private val mainMapViewModel: MainMapViewModel by viewModels()

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

        if (drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawer(Gravity.LEFT)
        }

        val toolbar = binding.MapConstraintLayout.toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar

        actionBar?.let {
            it.setHomeAsUpIndicator(R.drawable.pizza_white)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (drawer.isDrawerOpen(Gravity.LEFT)) {
                    drawer.closeDrawer(Gravity.LEFT)
                } else {
                    drawer.openDrawer(Gravity.LEFT)
                }
            }
        }
        return true
    }

    /**
     * Handles user events in the drawer layout
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favorites -> {
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

        map.setInfoWindowAdapter(PizzaInfoWindowAdapter(this))
        map.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
        map.setOnInfoWindowLongClickListener {
            handleLongInfoWindowClick(it)
        }

        setUpViewModel()
        setupMapListeners()
        getCurrentLocation()

        map.setOnCameraIdleListener(this)
        mainMapViewModel.requestForMorePizzaShops()
    }

    /**
     * When user long clicks on the info window it will go to a Google Search or a Web Search
     */
    private fun handleLongInfoWindowClick(marker: Marker) {
        val pizzaFav = marker.tag as PizzaFav
        val text = "${pizzaFav.name} ${pizzaFav.address}"
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
        val pizzaFav = marker.tag as PizzaFav

        removePizzaFav(pizzaFav)

        // Check if this is a favorite or not. If this is not a favorite, make it one.
        if (pizzaFav.favorite == 1) {
            mainMapViewModel.addPizza(pizzaFav.apply {
                favorite = 0
            })
        } else {
            mainMapViewModel.addPizza(pizzaFav.apply {
                favorite = 1
            })
        }
    }

    private fun removePizzaFav(pizzaFav: PizzaFav) {
        markerMap[pizzaFav.id]!!.remove()
        markerMap.remove(pizzaFav.id)
        pizzaMap.remove(pizzaFav.id)
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
            removeMarkers()
        }
    }

    private fun removeMarkers() {
        //Remove some farther pizza favs.
        val currentCameraLatLng = getCameraLatLng()
        pizzaMap.filter {
            mainMapViewModel.compareLatLng(
                LatLng(it.value.lat, it.value.lng),
                currentCameraLatLng
            )
        }.values.forEach {
            removePizzaFav(it)
        }
    }

    /**
     * Called inside onCameraIdle
     */
    private fun updateViewModel(newLocation: LatLng) {
        mainMapViewModel.updateLocation(newLocation)
    }

    /**
     * This is where the code draws the markers.
     */
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun setUpViewModel() {
        /**
         * Every time a pizza shop is given, draw it.
         */
        mainMapViewModel.repoPizzaFlow.onEach {
            if (!pizzaMap.containsKey(it.id)) {
                pizzaMap[it.id] = it
                makeMarkersFromPizzaFav()
            }
        }.launchIn(
            lifecycleScope
        )
    }

    /**
     * If a marker is not inside the map, add it.
     */
    private fun makeMarkersFromPizzaFav() {
        pizzaMap.forEach { pizzafav ->
            if (markerMap[pizzafav.key] == null) {
                addMarker(pizzafav.value)
            }
        }
    }

    private fun addMarker(pizzaFav: PizzaFav) {
        val isFav = pizzaFav.favorite == 1

        val icon = getIcon(isFav)

        markerMap[pizzaFav.id] = map.addMarker(
            MarkerOptions().position(
                LatLng(
                    pizzaFav.lat,
                    pizzaFav.lng
                )
            )
                .alpha(getAlpha(isFav))
                .title(pizzaFav.name)
                .snippet(pizzaFav.address)
                .icon(icon)
        )
        markerMap[pizzaFav.id]!!.tag = pizzaFav
    }

    private fun generateBitmapDescriptorFromRes(
        context: Context, resId: Int
    ): BitmapDescriptor? {
        val drawable: Drawable = context.getDrawable(resId)!!
        drawable.setBounds(
            0,
            0,
            drawable.intrinsicWidth,
            drawable.intrinsicHeight
        )
        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun getAlpha(isFav: Boolean) = if (isFav) 1f else defaultTransparency

    private fun getIcon(isFav: Boolean): BitmapDescriptor? {
        return if (isFav) generateBitmapDescriptorFromRes(this, R.drawable.pizza_favortie)
        else generateBitmapDescriptorFromRes(this, R.drawable.pizza_not_favortie)
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
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        zoom
                    )
                    map.moveCamera(cameraUpdate)
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

                if (location != null && !mainMapViewModel.hasLatestUserLocation) {
                    val latLng = LatLng(location.latitude, location.longitude)

                    val cameraUpdate =
                        CameraUpdateFactory.newLatLngZoom(
                            latLng,
                            zoom
                        )
                    map.moveCamera(cameraUpdate)
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

        //close the drawer if it is open
        val drawer: DrawerLayout = findViewById(R.id.MainActivityDrawerLayout)
        if (drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawer(Gravity.LEFT)
        }
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
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        favoriteZoom
                    )
                    map.moveCamera(cameraUpdate)
                } ?: return

            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION = 1
    }
}
