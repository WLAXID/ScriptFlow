package com.wlaxid.scriptflow.ui.overlay

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout

class SelectionOverlayView(
    context: Context,
    private val onSelectionChanged: (RectF) -> Unit
) : FrameLayout(context) {

    constructor(context: Context) : this(context, {})

    enum class Mode {
        EXPANDED,
        COLLAPSED
    }

    private var mode = Mode.EXPANDED

    private val dimPaint = Paint().apply {
        color = 0x99000000.toInt()
    }

    private val _selectionRect = RectF()

    val selectionRect: RectF
        get() = _selectionRect

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var dragMode = SelectionDragMode.NONE
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    val borderStrokeWidth = 8f

    private val dimPath = Path()
    private val rectPath = Path()

    private val cornerPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = borderStrokeWidth
        strokeCap = Paint.Cap.SQUARE
        isAntiAlias = true
    }

    private val cornerLength = 40f

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)

        if (_selectionRect.isEmpty) {
            val rectWidth = width * 0.6f
            val rectHeight = height * 0.3f
            val rectLeft = (width - rectWidth) / 2f
            val rectTop = height * 0.2f

            _selectionRect.set(
                rectLeft,
                rectTop,
                rectLeft + rectWidth,
                rectTop + rectHeight
            )

            onSelectionChanged(selectionRect)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {

        if (mode == Mode.EXPANDED) {

            dimPath.reset()
            rectPath.reset()

            dimPath.addRect(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                Path.Direction.CW
            )

            rectPath.addRect(_selectionRect, Path.Direction.CCW)

            dimPath.addPath(rectPath)

            canvas.drawPath(dimPath, dimPaint)

            drawCorners(canvas)
        }

        super.dispatchDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (mode == Mode.COLLAPSED) return false

        val x = event.x
        val y = event.y

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                dragMode = SelectionDragDetector.detect(
                    _selectionRect,
                    x,
                    y,
                    touchSlop
                )

                if (dragMode != SelectionDragMode.NONE) {
                    lastTouchX = x
                    lastTouchY = y
                    return true
                }

                return false
            }

            MotionEvent.ACTION_MOVE -> {

                if (dragMode == SelectionDragMode.NONE) return false

                val dx = x - lastTouchX
                val dy = y - lastTouchY

                resizeOrMove(dx, dy)

                lastTouchX = x
                lastTouchY = y

                return true
            }

            MotionEvent.ACTION_UP -> {
                if (dragMode != SelectionDragMode.NONE) {
                    performClick()
                }
                dragMode = SelectionDragMode.NONE
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                dragMode = SelectionDragMode.NONE
                return true
            }
        }

        return false
    }
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }


    fun moveSelection(dx: Float, dy: Float) {

        _selectionRect.offset(dx, dy)

        if (mode == Mode.EXPANDED) {

            if (_selectionRect.left < 0)
                _selectionRect.offset(-_selectionRect.left, 0f)

            if (_selectionRect.top < 0)
                _selectionRect.offset(0f, -_selectionRect.top)

            if (_selectionRect.right > width)
                _selectionRect.offset(width - _selectionRect.right, 0f)

            if (_selectionRect.bottom > height)
                _selectionRect.offset(0f, height - _selectionRect.bottom)
        }

        onSelectionChanged(_selectionRect)
    }

    fun setMode(newMode: Mode) {
        mode = newMode
        invalidate()
    }

    private fun resizeOrMove(dx: Float, dy: Float) {

        when (dragMode) {

            SelectionDragMode.MOVE -> {
                _selectionRect.offset(dx, dy)
            }

            SelectionDragMode.LEFT -> {
                _selectionRect.left += dx
            }

            SelectionDragMode.RIGHT -> {
                _selectionRect.right += dx
            }

            SelectionDragMode.TOP -> {
                _selectionRect.top += dy
            }

            SelectionDragMode.BOTTOM -> {
                _selectionRect.bottom += dy
            }

            SelectionDragMode.TOP_LEFT -> {
                _selectionRect.left += dx
                _selectionRect.top += dy
            }

            SelectionDragMode.TOP_RIGHT -> {
                _selectionRect.right += dx
                _selectionRect.top += dy
            }

            SelectionDragMode.BOTTOM_LEFT -> {
                _selectionRect.left += dx
                _selectionRect.bottom += dy
            }

            SelectionDragMode.BOTTOM_RIGHT -> {
                _selectionRect.right += dx
                _selectionRect.bottom += dy
            }

            else -> return
        }

        enforceBounds()
        onSelectionChanged(_selectionRect)
    }

    private fun enforceBounds() {

        val minSize = 228f

        // минимальная ширина
        if (_selectionRect.width() < minSize) {
            _selectionRect.right = _selectionRect.left + minSize
        }

        // минимальная высота
        if (_selectionRect.height() < minSize) {
            _selectionRect.bottom = _selectionRect.top + minSize
        }

        // границы экрана
        if (_selectionRect.left < 0f)
            _selectionRect.offset(-_selectionRect.left, 0f)

        if (_selectionRect.top < 0f)
            _selectionRect.offset(0f, -_selectionRect.top)

        if (_selectionRect.right > width)
            _selectionRect.offset(width - _selectionRect.right, 0f)

        if (_selectionRect.bottom > height)
            _selectionRect.offset(0f, height - _selectionRect.bottom)
    }

    private fun drawCorners(canvas: Canvas) {

        val left = _selectionRect.left
        val right = _selectionRect.right
        val top = _selectionRect.top
        val bottom = _selectionRect.bottom

        val l = cornerLength

        // левый верх
        canvas.drawLine(left, top, left + l, top, cornerPaint)
        canvas.drawLine(left, top, left, top + l, cornerPaint)

        // правый верх
        canvas.drawLine(right, top, right - l, top, cornerPaint)
        canvas.drawLine(right, top, right, top + l, cornerPaint)

        // левый низ
        canvas.drawLine(left, bottom, left + l, bottom, cornerPaint)
        canvas.drawLine(left, bottom, left, bottom - l, cornerPaint)

        // правый низ
        canvas.drawLine(right, bottom, right - l, bottom, cornerPaint)
        canvas.drawLine(right, bottom, right, bottom - l, cornerPaint)
    }

    fun applyRectImmediate(rect: RectF) {
        _selectionRect.set(rect)
        invalidate()
    }
}
