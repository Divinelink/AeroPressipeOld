package aeropresscipe.divinelink.aeropress.customviews

import aeropresscipe.divinelink.aeropress.databinding.ViewNotificationBinding
import aeropresscipe.divinelink.aeropress.util.ThreadUtil.runOnMain
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


class NotificationView : FrameLayout {
    private var binding = ViewNotificationBinding.inflate(LayoutInflater.from(context), this, false)

    private var timer = Executors.newScheduledThreadPool(1)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {

        addView(binding.root)
        initUI()
    }

    private fun initUI() {
        binding.notificationRoot.setOnClickListener {
            binding.notificationRoot.visibility = View.GONE
        }
    }

    fun showNotification(@StringRes resId: Int, length: NotificationLength) {
        binding.notificationRoot.visibility = VISIBLE
        binding.notificationText.text = context.getText(resId)

        scheduleTimer({ runOnMain { hideNotification() } }, length.delay)
    }

    private fun hideNotification() {
        binding.notificationRoot.visibility = View.GONE
    }

    private inline fun scheduleTimer(crossinline action: () -> Unit, duration: Long) {
        cancelTimer()
        timer.schedule({ action() }, duration, TimeUnit.MILLISECONDS)
    }

    private fun cancelTimer() {
        timer.shutdownNow()
        timer = Executors.newScheduledThreadPool(1)
    }


    companion object {
        private const val SHORT_DELAY = 3000L
        private const val LONG_DELAY = 5000L
    }
}