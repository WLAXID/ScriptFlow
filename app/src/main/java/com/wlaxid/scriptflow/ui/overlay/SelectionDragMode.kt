package com.wlaxid.scriptflow.ui.overlay

import android.graphics.RectF

enum class SelectionDragMode {
    NONE,
    MOVE,
    LEFT, RIGHT, TOP, BOTTOM,
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

object SelectionDragDetector {

    fun detect(
        rect: RectF,
        x: Float,
        y: Float,
        touchSlop: Float
    ): SelectionDragMode {

        val left = rect.left
        val right = rect.right
        val top = rect.top
        val bottom = rect.bottom

        val nearLeft = kotlin.math.abs(x - left) < touchSlop
        val nearRight = kotlin.math.abs(x - right) < touchSlop
        val nearTop = kotlin.math.abs(y - top) < touchSlop
        val nearBottom = kotlin.math.abs(y - bottom) < touchSlop

        val withinVertical = y in (top - touchSlop)..(bottom + touchSlop)
        val withinHorizontal = x in (left - touchSlop)..(right + touchSlop)

        return when {

            // углы
            nearLeft && nearTop -> SelectionDragMode.TOP_LEFT
            nearRight && nearTop -> SelectionDragMode.TOP_RIGHT
            nearLeft && nearBottom -> SelectionDragMode.BOTTOM_LEFT
            nearRight && nearBottom -> SelectionDragMode.BOTTOM_RIGHT

            // стороны - только если палец в пределах противоположной оси
            nearLeft && withinVertical -> SelectionDragMode.LEFT
            nearRight && withinVertical -> SelectionDragMode.RIGHT
            nearTop && withinHorizontal -> SelectionDragMode.TOP
            nearBottom && withinHorizontal -> SelectionDragMode.BOTTOM

            // внутри - перемещение
            rect.contains(x, y) -> SelectionDragMode.MOVE

            else -> SelectionDragMode.NONE
        }
    }
}