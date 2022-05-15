package aeropresscipe.divinelink.aeropress.customviews

import aeropresscipe.divinelink.aeropress.databinding.ViewNotificationBinding
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

enum class NotificationLength(val delay: Long) {
    SHORT(3000L),
    LONG(5000L)
}

//var myHandler = Handler(Looper.getMainLooper())

class NotificationView : FrameLayout {
    private var binding = ViewNotificationBinding.inflate(LayoutInflater.from(context), this, false)

    private var executor = Executors.newSingleThreadScheduledExecutor()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        binding.root.let { view ->
            addView(view)
            initUI()
        }
    }

    private fun initUI() {
        binding.notificationRoot.setOnClickListener {
            executor.shutdownNow()
            binding.notificationRoot.visibility = View.GONE
        }
    }

    fun showNotification(@StringRes resId: Int, length: NotificationLength) {
        executor.shutdownNow()
        executor = Executors.newSingleThreadScheduledExecutor()

        binding.notificationRoot.visibility = View.VISIBLE
        binding.notificationText.text = context.getText(resId)
        executor.schedule(hideRunnable(), length.delay, TimeUnit.MILLISECONDS)
    }

    private fun hideRunnable() = Runnable {
        binding.notificationRoot.visibility = View.GONE
    }



    companion object {
        private const val SHORT_DELAY = 3000L
        private const val LONG_DELAY = 5000L
    }
}