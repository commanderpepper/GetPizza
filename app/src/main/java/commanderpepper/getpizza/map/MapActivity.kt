package commanderpepper.getpizza.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import commanderpepper.getpizza.R
import commanderpepper.getpizza.retrofit.ZomatoConstants.Companion.MY_PERMISSIONS_REQUEST_FINE_LOCATION


class MapActivity : AppCompatActivity() {

    private var latitude: Double = 40.7128
    private var longitude: Double = -74.0060

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_FINE_LOCATION
            )
        } else {

            //TODO call some method or object to get a location and / or instantiate the view (fragment)
            setUpGoogleMapView()
            Log.d("Humza", "$longitude $latitude")
        }


    }

    @SuppressLint("MissingPermission")
    fun setUpGoogleMapView() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        longitude = location.longitude
        latitude = location.latitude

        val bundle = Bundle()
        bundle.putDouble("latitude", latitude)
        bundle.putDouble("longitude", longitude)

        val pizzaMap = GoogleMapView().apply { arguments = bundle }
//            pizzaMap.arguments = bundle

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.map_container, pizzaMap)
        fragmentTransaction.commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED)) {
                    setUpGoogleMapView()
                } else {
                    val frmLayout = findViewById<FrameLayout>(R.id.map_container)
                    val view = TextView(frmLayout.context).apply {
                        text = getText(R.string.location_permission_text)
                        gravity = 1
                    }
                    view.text = getText(R.string.location_permission_text)
                    frmLayout.addView(view)
                    println("darn")
                }
            }
        }
    }

}
