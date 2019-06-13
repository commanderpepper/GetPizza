package commanderpepper.getpizza.map

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import commanderpepper.getpizza.R
import commanderpepper.getpizza.retrofit.ZomatoService
import io.reactivex.disposables.Disposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.location.LocationManager
import android.content.Context.LOCATION_SERVICE




class MapActivity : AppCompatActivity() {

    val zomatoService by lazy {
        ZomatoService.create()
    }

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val pizzaMap = GoogleMapView()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.map_container, pizzaMap)
        fragmentTransaction.commit()

        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val longitude = location.longitude
        val latitude = location.latitude
    }

}
