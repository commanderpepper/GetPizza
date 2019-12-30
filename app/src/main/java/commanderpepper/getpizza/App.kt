package commanderpepper.getpizza

import android.app.Application
import commanderpepper.getpizza.repository.PizzaRepository
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        PizzaRepository.initialize(this)
    }
}