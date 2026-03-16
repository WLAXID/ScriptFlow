package com.wlaxid.scriptflow.ui.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.graphics.toColorInt

class OverlayToast(private val context: Context) {

    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: View? = null

    fun show(message: String, duration: Long = 2000L) {
        if (view != null) return

        val textView = TextView(context).apply {
            text = message
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(40, 24, 40, 24)
            background = GradientDrawable().apply {
                setColor("#CC000000".toColorInt())
                cornerRadius = 32f
            }
            elevation = 20f
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 200
        }

        view = textView
        wm.addView(textView, params)

        Handler(Looper.getMainLooper()).postDelayed({
            hide()
        }, duration)
    }

    private fun hide() {
        view?.let {
            try {
                wm.removeView(it)
            } catch (_: Exception) {}
        }
        view = null
    }
}