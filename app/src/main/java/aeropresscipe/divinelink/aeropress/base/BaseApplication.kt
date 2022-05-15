package aeropresscipe.divinelink.aeropress.base

import aeropresscipe.divinelink.aeropress.helpers.SharedPreferences
import android.app.Application

val preferences: SharedPreferences by lazy {
    BaseApplication.sharedPreferences
}

class BaseApplication : Application() {

    companion object {
        lateinit var instance: BaseApplication
            private set
        lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        sharedPreferences = SharedPreferences(this)
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}