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
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    /**
     * Longitude and Latitude of New York City
     */
    private var latitude: Double = 40.7128
    private var longitude: Double = -74.0060

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
//        askForPermission()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        getCurrentLocation()
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

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

//    private fun askForPermission() {
//        if (ContextCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_FINE_LOCATION
//            )
//        } else {
//            setUpGoogleMapView()
//            Log.d("Humza", "$longitude $latitude")
//        }
//    }

    override fun onStart() {
        super.onStart()
//        setUpGoogleMapView()
    }

//    @SuppressLint("MissingPermission")
//    fun setUpGoogleMapView() {
//        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//
//        val fragmentTransaction = supportFragmentManager.beginTransaction()
//
//        if (supportFragmentManager.fragments.isEmpty()) {
//            val pizzaMap = GoogleMapView().apply {
//                this.lm = lm
//                this.location = location
//            }
//
//            Log.d("Humza", fragmentTransaction.isEmpty.toString())
//
//            fragmentTransaction.add(R.id.map_container, pizzaMap)
//            Log.d("Humza", supportFragmentManager.fragments.toString())
//            fragmentTransaction.commit()
//
//        } else {
//            val pm = supportFragmentManager.fragments.first() as GoogleMapView
//            pm.apply {
//                this.lm = lm
//                this.location = location
//            }
//        }
//    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        when (requestCode) {
//            MY_PERMISSIONS_REQUEST_FINE_LOCATION -> {
//                if ((grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED)) {
//                    setUpGoogleMapView()
//                } else {
//                    val frmLayout = findViewById<FrameLayout>(R.id.map_container)
//                    val view = TextView(frmLayout.context).apply {
//                        text = getText(R.string.location_permission_text)
//                        gravity = 1
//                    }
//                    frmLayout.addView(view)
//                    println("darn")
//                }
//            }
//        }
//    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }
}
