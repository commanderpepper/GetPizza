package commanderpepper.getpizza.map


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.gms.maps.MapsInitializer

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.GoogleMap

import commanderpepper.getpizza.R

class GoogleMapView : Fragment() {
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var viewmodel : MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        viewmodel = MapViewModel()

        val rootView = inflater.inflate(R.layout.fragment_google_map_view, container, false)

        mapView = rootView.findViewById(R.id.pmap) as MapView
        mapView.onCreate(savedInstanceState)

        mapView.onResume()

        try {
            MapsInitializer.initialize(activity!!.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mapView.getMapAsync { mMap ->
            googleMap = mMap

            // For dropping a marker at a point on the Map
            val sydney = LatLng(40.8426000000, -73.2883000000)
            googleMap.addMarker(MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"))

            // For zooming automatically to the location of the marker
            val cameraPosition = CameraPosition.Builder().target(sydney).zoom(18f).build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }


        return rootView
    }

    override fun onResume() {
        super.onResume()
//        viewmodel.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
//        viewmodel.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
//        viewmodel.onResume()
        mapView.onDestroy()
    }
}
