package commanderpepper.getpizza.map

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.SupportMapFragment
import commanderpepper.getpizza.R

class MapView : SupportMapFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        println("hi")
        return inflater.inflate(R.layout.fragement_map, container, false)
    }
}