package aeropresscipe.divinelink.aeropress.customviews

import aeropresscipe.divinelink.aeropress.databinding.ViewNotificationBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.google.android.material.snackbar.ContentViewCallback

class NotificationView : FrameLayout, ContentViewCallback {
    private var binding = ViewNotificationBinding.inflate(LayoutInflater.from(context), this, false)

    override fun animateContentIn(delay: Int, duration: Int) {
        // todo animation
    }

    override fun animateContentOut(delay: Int, duration: Int) {
        // todo animation
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {

        addView(binding.root)
    }

    fun setText(@StringRes resId: Int) {
        setText(context.getText(resId))
    }

    fun setText(text: CharSequence) {
        binding.notificationText.text = text
    }

    companion object {
        private const val SHORT_DELAY = 3000L
        private const val LONG_DELAY = 5000L
    }
}