package com.wlaxid.scriptflow.ui.console

import android.animation.ValueAnimator
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.view.doOnLayout
import com.wlaxid.scriptflow.R

class ConsoleController(
    private val root: View
) {

    private val colorOutput = 0xFF00FF7F.toInt()   // зелёный
    private val colorSystem = 0xFF888888.toInt()   // серый
    private val colorError  = 0xFFFF5555.toInt()   // красный
    private val output: TextView = root.findViewById(R.id.consoleOutput)
    private val handle: View = root.findViewById(R.id.consoleHandle)
    private val title: View = root.findViewById(R.id.consoleTitle)
    private val minHeight: Int get() = handle.height
    private var maxHeight = 0
    private var currentHeight = 0
    private var startY = 0f
    private var startHeight = 0
    private var dragging = false
    private val partialLine = StringBuilder()

    init {
        root.doOnLayout {
            maxHeight = (root.parent as View).height

            if (currentHeight == 0) {
                currentHeight = minHeight
            }

            setHeight(currentHeight)
        }
        root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            maxHeight = (root.parent as View).height
            currentHeight = currentHeight.coerceIn(minHeight, maxHeight)
            setHeight(currentHeight)
        }

        setupDrag()
    }

    fun clear() {
        output.text = ""
    }

    fun appendMessage(
        text: String,
        type: ConsoleMessageType,
        newline: Boolean = true
    ) {
        val finalText = if (newline) text + "\n" else text
        val span = SpannableString(finalText)

        when (type) {
            ConsoleMessageType.OUTPUT ->
                span.setSpan(ForegroundColorSpan(colorOutput), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            ConsoleMessageType.SYSTEM -> {
                span.setSpan(ForegroundColorSpan(colorSystem), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            ConsoleMessageType.ERROR ->
                span.setSpan(ForegroundColorSpan(colorError), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        output.append(span)
    }

    fun expand() {
        animateTo(maxHeight)
    }

    fun hide() {
        animateTo(minHeight)
    }

    private fun setupDrag() {
        handle.setOnTouchListener { _, event ->
            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    startHeight = currentHeight
                    dragging = true
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!dragging) return@setOnTouchListener false

                    val dy = startY - event.rawY
                    val newHeight = (startHeight + dy).toInt()
                        .coerceIn(minHeight, maxHeight)

                    currentHeight = newHeight
                    setHeight(currentHeight)
                    true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    dragging = false
                    handle.performClick()
                    true
                }

                else -> false
            }
        }
    }
    private fun animateTo(target: Int) {
        val start = currentHeight
        if (start == target) return

        ValueAnimator.ofInt(start, target).apply {
            duration = 220
            addUpdateListener {
                currentHeight = it.animatedValue as Int
                setHeight(currentHeight)
            }
            start()
        }
    }

    private fun setHeight(height: Int) {
        val lp = root.layoutParams
        lp.height = height
        root.layoutParams = lp
        title.alpha = if (height <= minHeight + dp(4)) 0f else 1f
        title.isClickable = height > minHeight + dp(4)
    }

    private fun dp(value: Int): Int =
        (value * root.resources.displayMetrics.density).toInt()

    fun appendFragment(text: String, type: ConsoleMessageType) {
        val clean = text.replace("\r", "")
        var start = 0

        while (true) {
            val idx = clean.indexOf('\n', start)
            if (idx == -1) {
                // нет конца строки - копим
                partialLine.append(clean.substring(start))
                break
            }

            // есть завершённая строка
            partialLine.append(clean.substring(start, idx))
            appendMessage(partialLine.toString(), type)
            partialLine.setLength(0)

            start = idx + 1
            if (start >= clean.length) break
        }
    }
}