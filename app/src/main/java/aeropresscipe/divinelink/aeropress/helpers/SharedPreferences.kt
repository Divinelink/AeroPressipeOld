package aeropresscipe.divinelink.aeropress.helpers

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)

    var isBrewing: Boolean
        get() = preferences.getBoolean(IS_BREWING, false)
        set(value) = preferences.edit().putBoolean(IS_BREWING, value).apply()

    companion object {
        private const val IS_BREWING = "IS_BREWING"
    }
}
