package commanderpepper.getpizza.map

import android.os.Bundle
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class PizzaSupportMap(): SupportMapFragment(){

    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)

    }

    override fun getMapAsync(callback: OnMapReadyCallback?) {
        //This is called when the map is ready 
        super.getMapAsync(callback)
    }
}