package commanderpepper.getpizza.map

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import commanderpepper.getpizza.R


class MapActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val pizzaMap = GoogleMapView()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.map_container, pizzaMap)
        fragmentTransaction.commit()
    }

}
