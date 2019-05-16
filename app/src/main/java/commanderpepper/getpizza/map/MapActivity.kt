package commanderpepper.getpizza.map

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import commanderpepper.getpizza.R

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as Map_View
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.map_fragment, mapFragment).commit()
//        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val map = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-33.8, -60.1)
//        val newyork = LatLng(40.7, -73.9)
        map.addMarker(MarkerOptions().position(sydney).title("Test"))
    }
}
