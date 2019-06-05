package commanderpepper.getpizza.map

import android.util.Log
import commanderpepper.getpizza.BaseViewModel
import commanderpepper.getpizza.models.Location
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class MapViewModel : BaseViewModel {

    override fun onCreate() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResume() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

//    fun getLatLngObservable() : Observable<Pair<Double, Double>> {
//
//    }

}

class LocationObserver : Observer<Location>{
    override fun onComplete() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSubscribe(d: Disposable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNext(location: Location) {
        Log.d("Humza", "${location.latitude} ${location.longitude}"  )
    }

    override fun onError(e: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}