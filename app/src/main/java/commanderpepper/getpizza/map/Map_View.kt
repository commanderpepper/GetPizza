package commanderpepper.getpizza.map

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import commanderpepper.getpizza.R



class Map_View : SupportMapFragment(), OnMapReadyCallback {


//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        println("hi")
//        return inflater.inflate(R.layout.fragement_map, container, false)
//    }

    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View? {
        return inflater.inflate(commanderpepper.getpizza.R.layout.fragement_map, container, false)
//        this.getMapAsync()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        mapView = view.findViewById<MapView>()
        mapView = view.findViewById(commanderpepper.getpizza.R.id.map) as MapView
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    override fun onMapReady(g_map: GoogleMap) {
        val map = g_map
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-33.8, -60.1)
//        val newyork = LatLng(40.7, -73.9)
        map.addMarker(MarkerOptions().position(sydney).title("Test"))

        googleMap = map
    }


}