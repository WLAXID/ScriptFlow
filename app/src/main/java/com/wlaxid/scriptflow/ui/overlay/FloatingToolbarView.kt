package com.wlaxid.scriptflow.ui.overlay

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.wlaxid.scriptflow.R
import kotlin.math.abs

class FloatingToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val btnToggle: ImageView
    private val btnSave: ImageView
    private val btnCancel: ImageView
    private val notch: View

    private var lastRawX = 0f
    private var lastRawY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downRawX = 0f
    private var downRawY = 0f
    private var dragging = false

    private var onToggle: (() -> Unit)? = null
    private var onDrag: ((dx: Float, dy: Float) -> Unit)? = null
    private var onClose: (() -> Unit)? = null
    private var onSave: (() -> Unit)? = null

    private val resolutionLabel: TextView
    init {
        LayoutInflater.from(context).inflate(R.layout.overlay_toolbar, this, true)
        btnToggle = findViewById(R.id.btnToggle)
        btnSave = findViewById(R.id.btnSaveImage)
        btnCancel = findViewById(R.id.btnCancel)
        resolutionLabel = findViewById(R.id.resolutionLabel)
        notch = findViewById(R.id.notchBackground)

        btnToggle.setOnClickListener { onToggle?.invoke() }
        btnCancel.setOnClickListener { onClose?.invoke() }
        btnSave.setOnClickListener { onSave?.invoke() }


        setOnTouchListener { v, ev ->
            when (ev.actionMasked) {

                MotionEvent.ACTION_DOWN -> {
                    downRawX = ev.rawX
                    downRawY = ev.rawY
                    lastRawX = ev.rawX
                    lastRawY = ev.rawY
                    dragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dxTotal = ev.rawX - downRawX
                    val dyTotal = ev.rawY - downRawY

                    if (!dragging) {
                        if (abs(dxTotal) > touchSlop || abs(dyTotal) > touchSlop) {
                            dragging = true
                        } else {
                            return@setOnTouchListener true
                        }
                    }

                    val dx = ev.rawX - lastRawX
                    val dy = ev.rawY - lastRawY
                    lastRawX = ev.rawX
                    lastRawY = ev.rawY

                    onDrag?.invoke(dx, dy)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!dragging) {
                        v.performClick()
                    }
                    dragging = false
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    dragging = false
                    true
                }

                else -> false
            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun setOnToggle(cb: () -> Unit) { onToggle = cb }
    fun setOnDrag(cb: (Float, Float) -> Unit) { onDrag = cb }
    fun setOnClose(cb: () -> Unit) { onClose = cb }
    fun setOnSave(cb: () -> Unit) { onSave = cb }

    fun setCollapsed(collapsed: Boolean) {

        val targetRotation = if (collapsed) 180f else 0f

        if (collapsed) {

            btnToggle.animate()
                .rotationBy(targetRotation)
                .setDuration(100L)
                .withEndAction {
                    btnToggle.setImageResource(R.drawable.ic_overlay_closed)
                    btnToggle.animate()
                        .setDuration(100L)
                        .start()
                }
                .start()

            btnSave.visibility = GONE
            findViewById<View>(R.id.divider2)?.visibility = GONE
            findViewById<View>(R.id.notchBackground)?.visibility = GONE
            findViewById<View>(R.id.resolutionLabel)?.visibility = INVISIBLE

            animate()
                .alpha(1f)
                .scaleX(0.85f)
                .scaleY(0.85f)
                .translationY(-20f)
                .setDuration(180L)
                .withEndAction {
                    scaleX = 1f
                    scaleY = 1f
                    translationY = 0f
                }
                .start()

        } else {

            btnToggle.animate()
                .rotationBy(targetRotation)
                .setDuration(100L)
                .withEndAction {
                    btnToggle.setImageResource(R.drawable.ic_overlay_opened)
                    btnToggle.animate()
                        .setDuration(100L)
                        .start()
                }
                .start()

            btnSave.visibility = VISIBLE
            findViewById<View>(R.id.divider2)?.visibility = VISIBLE
            findViewById<View>(R.id.notchBackground)?.visibility = VISIBLE
            findViewById<View>(R.id.resolutionLabel)?.visibility = VISIBLE

            scaleX = 0.85f
            scaleY = 0.85f
            translationY = -20f

            animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(180L)
                .start()
        }
    }

    fun setResolutionText(text: String) {
        resolutionLabel.text = text
    }

    override fun hasOverlappingRendering(): Boolean = false

}