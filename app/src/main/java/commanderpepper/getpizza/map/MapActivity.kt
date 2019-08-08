package commanderpepper.getpizza.map

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import commanderpepper.getpizza.R
import android.Manifest
import androidx.databinding.DataBindingUtil
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import commanderpepper.getpizza.databinding.ActivityMapBinding
import commanderpepper.getpizza.retrofit.FourSquareService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import androidx.lifecycle.ViewModelProviders
import commanderpepper.getpizza.viewmodel.MainMapViewModel
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import kotlinx.coroutines.InternalCoroutinesApi


class MapActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favorites -> Log.d("Nav", "Clicked on fav")
        }
        return true
    }

    @InternalCoroutinesApi
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setUpViewModel()

        getCurrentLocation()
        getCurrentMapLocation()

        setupMapListeners()
    }

    private fun getCurrentMapLocation() {
        mainMapViewModel.setMapLocation(
            LatLng(
                map.cameraPosition.target.latitude,
                map.cameraPosition.target.longitude
            )
        )
    }

    @InternalCoroutinesApi
    private fun setUpViewModel() {
        mainMapViewModel = ViewModelProviders.of(this).get(MainMapViewModel::class.java)
//        mainMapViewModel.setUpFlow()
    }

    @InternalCoroutinesApi
    private fun setupMapListeners() {
        map.setOnCameraMoveListener {
            Log.d("MAP", "The map moved")
            Log.d("USER", mainMapViewModel.getUserLocation().toString())
            getCurrentMapLocation()
            mainMapViewModel.setUpFlow()
            Log.d("MAP", mainMapViewModel.getMapLocation().toString())
        }
        map.setOnMyLocationButtonClickListener {
            //TODO I think that this should call a different method that will call the view model
            getCurrentLocation()
            true
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

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
                    mainMapViewModel.setUserLocation(LatLng(location.latitude, location.longitude))
//                    mainMapViewModel.setUpFlow()
//                    mainMapViewModel.activateFlow()
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

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
