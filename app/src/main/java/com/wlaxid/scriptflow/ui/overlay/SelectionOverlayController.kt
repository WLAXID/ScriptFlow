package com.wlaxid.scriptflow.ui.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.RectF
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.view.Choreographer
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

class SelectionOverlayController(
    private val context: Context
) {

    private var projectionCallback: MediaProjection.Callback? = null
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var fullscreenView: SelectionOverlayView? = null
    private var toolbarView: FloatingToolbarView? = null
    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: android.hardware.display.VirtualDisplay? = null
    private var lastToolbarX = Int.MIN_VALUE
    private var lastToolbarY = Int.MIN_VALUE
    private var pendingRect: RectF? = null
    private var frameScheduled = false

    private val frameCallback = Choreographer.FrameCallback {

        frameScheduled = false

        val rect = pendingRect ?: return@FrameCallback
        pendingRect = null

        fullscreenView?.applyRectImmediate(rect)
        fullscreenView?.invalidate()

        updateToolbarPositionImmediate(rect)
    }

    private val fullscreenParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }

    private val toolbarParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }

    fun setMediaProjection(projection: MediaProjection) {

        if (mediaProjection != null) return

        mediaProjection = projection

        projectionCallback = object : MediaProjection.Callback() {
            override fun onStop() {
                virtualDisplay?.release()
                imageReader?.close()
                virtualDisplay = null
                imageReader = null
                mediaProjection = null
            }
        }

        projection.registerCallback(projectionCallback!!, null)

        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(
            width,
            height,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = projection.createVirtualDisplay(
            "screen_capture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface,
            null,
            null
        )
    }

    private val overlayToast = OverlayToast(context)

    fun show() {
        if (fullscreenView != null) return

        fullscreenView = SelectionOverlayView(context) { rect ->
            updateToolbarPosition(rect)
            updateResolutionLabel(rect)
        }

        toolbarView = FloatingToolbarView(context).apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            visibility = View.INVISIBLE
            setOnToggle { toggleCollapsed() }
            setOnDrag { dx, dy ->
                fullscreenView?.moveSelection(dx, dy)
            }
            setOnClose { hide() }
            setOnSave { fullscreenView?.selectionRect?.let { captureSelection(it) } }
        }

        windowManager.addView(fullscreenView, fullscreenParams)
        windowManager.addView(toolbarView, toolbarParams)

        val toolbar = toolbarView ?: return

        toolbar.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )

        fullscreenView?.let { updateToolbarPosition(it.selectionRect) }
    }

    fun hide() {
        toolbarView?.let { windowManager.removeView(it) }
        fullscreenView?.let { windowManager.removeView(it) }
        toolbarView = null
        fullscreenView = null
        virtualDisplay?.release()
        imageReader?.close()
        virtualDisplay = null
        imageReader = null
    }

    fun stopCaptureSession() {
        virtualDisplay?.release()
        imageReader?.close()
        virtualDisplay = null
        imageReader = null
    }

    private var collapsed = false

    private fun toggleCollapsed() {

        collapsed = !collapsed

        if (collapsed) {
            fullscreenParams.flags =
                fullscreenParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        } else {
            fullscreenParams.flags =
                fullscreenParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        }

        fullscreenView?.setMode(
            if (collapsed)
                SelectionOverlayView.Mode.COLLAPSED
            else
                SelectionOverlayView.Mode.EXPANDED
        )

        toolbarView?.setCollapsed(collapsed)

        val toolbar = toolbarView ?: return

        val density = context.resources.displayMetrics.density

        val expandedHeight = (56 * density).toInt()
        val collapsedHeight = (40 * density).toInt()

        toolbarParams.height = if (collapsed) collapsedHeight else expandedHeight

        // пересчитываем ширину wrap_content
        toolbar.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )

        toolbarParams.width = toolbar.measuredWidth



        toolbar.requestLayout()

        toolbar.post {
            fullscreenView?.let { updateToolbarPosition(it.selectionRect) }
        }

        windowManager.updateViewLayout(toolbar, toolbarParams)
        fullscreenView?.let { windowManager.updateViewLayout(it, fullscreenParams) }
    }

    private fun updateToolbarPosition(rect: RectF) {

        pendingRect = RectF(rect)

        if (!frameScheduled) {
            frameScheduled = true
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    private fun updateToolbarPositionImmediate(selectionRect: RectF) {

        val toolbar = toolbarView ?: return
        val fullscreen = fullscreenView ?: return

        val toolbarWidth = toolbarParams.width.takeIf { it > 0 }
            ?: toolbar.measuredWidth

        val toolbarHeight = toolbar.measuredHeight

        val centerX = selectionRect.centerX()
        val x = (centerX - toolbarWidth / 2f).toInt()

        val rectBottom = selectionRect.bottom
        val rectTop = selectionRect.top

        val insets = fullscreen.rootWindowInsets
        val systemBars = insets?.getInsets(android.view.WindowInsets.Type.systemBars())

        val bottomInset = systemBars?.bottom ?: 0
        val topInset = systemBars?.top ?: 0

        val overlayHeight = fullscreen.height - bottomInset

        var y = rectBottom.toInt()

        // если снизу не помещается - переносим над выделением
        if (!collapsed && rectBottom + toolbarHeight > overlayHeight) {
            y = (rectTop - toolbarHeight).toInt()
        }

        // защита от залета под статусбар
        if (y < topInset) {
            y = topInset
        }

        if (x != lastToolbarX || y != lastToolbarY) {

            toolbarParams.x = x
            toolbarParams.y = y

            windowManager.updateViewLayout(toolbar, toolbarParams)

            lastToolbarX = x
            lastToolbarY = y
        }

        if (toolbar.visibility != View.VISIBLE) {
            toolbar.visibility = View.VISIBLE
        }
    }

    private fun updateResolutionLabel(rect: RectF) {

        val toolbar = toolbarView ?: return

        val width = rect.width().toInt()
        val height = rect.height().toInt()

        toolbar.setResolutionText("$width × $height")
    }

    private fun captureSelection(rect: RectF) {

        val reader = imageReader ?: return

        val image = reader.acquireLatestImage() ?: return

        val width = image.width
        val height = image.height

        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width

        val bitmap = createBitmap(width + rowPadding / pixelStride, height)

        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        val location = IntArray(2)
        fullscreenView?.getLocationOnScreen(location)
        val overlayOffsetY = location[1]

        val stroke = fullscreenView?.borderStrokeWidth ?: 0f
        val inset = stroke / 2f + 1f // суммируется компенсация (остаток от сглаживания антиалиасинга)

        val cropped = Bitmap.createBitmap(
            bitmap,
            (rect.left + inset).toInt(),
            (rect.top + overlayOffsetY + inset).toInt(),
            (rect.width() - inset * 2).toInt(),
            (rect.height() - inset * 2).toInt()
        )

        saveBitmap(cropped)
    }

    private fun saveBitmap(bitmap: Bitmap) {

        try {

            val imagesDir = File(
                context.getExternalFilesDir(null),
                "images"
            )

            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val file = File(
                imagesDir,
                "screenshot_${System.currentTimeMillis()}.png"
            )

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            overlayToast.show("Скрин сохранён")

        } catch (e: Exception) {
            overlayToast.show("Ошибка сохранения: " + e.message)
        }
    }


}