package aeropresscipe.divinelink.aeropress.customviews

import aeropresscipe.divinelink.aeropress.R
import aeropresscipe.divinelink.aeropress.databinding.ViewNotificationBinding
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.RequiresApi

enum class NotificationLength(val delay: Long) {
    SHORT(1500L),
    LONG(3000L)
}

class NotificationView : FrameLayout {
    private var binding = ViewNotificationBinding.inflate(LayoutInflater.from(context), this, false)

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
        binding.root.setOnClickListener {
            binding.root.visibility = View.GONE
        }
    }

    fun showNotification(length: NotificationLength) {
        binding.root.visibility = View.VISIBLE

        when (length) {
            NotificationLength.SHORT -> Handler(Looper.getMainLooper()).postDelayed(hideRunnable(), length.delay)
            NotificationLength.LONG -> Handler(Looper.getMainLooper()).postDelayed(hideRunnable(), length.delay)
        }
    }

    private fun hideRunnable() = Runnable {
        binding.root.visibility = View.GONE
    }

    fun setNotificationText(text: String) {
        binding.notificationText.text = text
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showPopupWindow(anchor: View) {
        PopupWindow(anchor.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor.context)
            contentView = inflater.inflate(R.layout.popup_layout, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }
        }.also { popupWindow ->
            // Absolute location of the anchor view
            val location = IntArray(2).apply {
                anchor.getLocationOnScreen(this)
            }
            val size = Size(
                popupWindow.contentView.measuredWidth,
                popupWindow.contentView.measuredHeight
            )
            popupWindow.showAtLocation(
                anchor,
                Gravity.TOP or Gravity.START,
                location[0] - (size.width - anchor.width) / 2,
                location[1] - size.height
            )
        }
    }


    companion object {
        private const val SHORT_DELAY = 1500L
        private const val LONG_DELAY = 3000L
    }
}