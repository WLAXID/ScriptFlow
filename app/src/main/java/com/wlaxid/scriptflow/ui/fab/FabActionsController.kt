package com.wlaxid.scriptflow.ui.fab

import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wlaxid.scriptflow.R
import androidx.core.view.children

class FabActionsController(
    root: View,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onSearch: () -> Unit = {},
    onScreenshot: () -> Unit = {},
    onEyedropper: () -> Unit = {}
) {

    private val miniContainer: ViewGroup =
        root.findViewById(R.id.fabMiniContainer)
    private val fabActions: FloatingActionButton =
        root.findViewById(R.id.fabActions)

    private val fabUndo: FloatingActionButton =
        root.findViewById(R.id.fabUndo)

    private val fabRedo: FloatingActionButton =
        root.findViewById(R.id.fabRedo)

    private val fabSearch: FloatingActionButton =
        root.findViewById(R.id.fabSearch)

    private val fabScreenshot: FloatingActionButton =
        root.findViewById(R.id.fabScreenshot)

    private val fabEyedropper: FloatingActionButton =
        root.findViewById(R.id.fabEyedropper)

    private var opened = false

    init {
        fabUndo.setOnClickListener { onUndo(); close() }
        fabRedo.setOnClickListener { onRedo(); close() }
        fabSearch.setOnClickListener { onSearch(); close() }
        fabScreenshot.setOnClickListener { onScreenshot(); close() }
        fabEyedropper.setOnClickListener { onEyedropper(); close() }

        fabActions.setOnClickListener {
            if (opened) close() else open()
        }
    }

    private fun open() {
        opened = true

        // меняем иконку СРАЗУ
        fabActions.setImageResource(R.drawable.ic_settings_2)

        fabActions.animate()
            .rotationBy(360f)
            .setDuration(300)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .start()

        miniContainer.visibility = View.VISIBLE
        miniContainer.alpha = 1f

        miniContainer.children
            .toList()
            .reversed()
            .forEachIndexed { index, view ->
                view.scaleX = 0f
                view.scaleY = 0f
                view.alpha = 0f

                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setStartDelay(index * 40L)
                    .setDuration(180)
                    .setInterpolator(android.view.animation.OvershootInterpolator())
                    .start()
            }
    }

    private fun close() {
        opened = false

        // возвращаем иконку
        fabActions.setImageResource(R.drawable.ic_settings)

        fabActions.animate()
            .rotationBy(360f)
            .setDuration(260)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        miniContainer.children.forEachIndexed { index, view ->
            view.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setStartDelay(index * 30L)
                .setDuration(120)
                .withEndAction {
                    if (index == miniContainer.childCount - 1) {
                        miniContainer.visibility = View.GONE
                    }
                }
                .start()
        }
    }

    fun collapse() {
        if (opened) close()
    }
}