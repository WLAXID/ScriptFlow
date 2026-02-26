package com.wlaxid.scriptflow.ui.actions

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wlaxid.scriptflow.R
import com.wlaxid.scriptflow.runtime.RunState

class EditorActionsController(
    toolbarRoot: View,
    fabRoot: View,
    private val drawerLayout: DrawerLayout,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onSearch: () -> Unit = {},
    onScreenshot: () -> Unit = {},
    onEyedropper: () -> Unit = {},
    onExecute: () -> Unit = {}
) {

    private val btnOptions: ImageView =
        toolbarRoot.findViewById(R.id.btnOptions)

    private val txtFileName: TextView =
        toolbarRoot.findViewById(R.id.txtFileName)

    private val fabExecute: FloatingActionButton =
        fabRoot.findViewById(R.id.fabExecute)

    private val fabMenu: FloatingActionButton =
        fabRoot.findViewById(R.id.fabActions)

    private val miniContainer: ViewGroup =
        fabRoot.findViewById(R.id.fabMiniContainer)

    private val fabUndo: FloatingActionButton =
        fabRoot.findViewById(R.id.fabUndo)

    private val fabRedo: FloatingActionButton =
        fabRoot.findViewById(R.id.fabRedo)

    private val fabSearch: FloatingActionButton =
        fabRoot.findViewById(R.id.fabSearch)

    private val fabScreenshot: FloatingActionButton =
        fabRoot.findViewById(R.id.fabScreenshot)

    private val fabEyedropper: FloatingActionButton =
        fabRoot.findViewById(R.id.fabEyedropper)

    private var menuOpened = false

    init {
        btnOptions.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        fabExecute.setOnClickListener { onExecute() }

        fabMenu.setOnClickListener {
            if (menuOpened) closeMenu() else openMenu()
        }

        fabUndo.setOnClickListener { onUndo(); }
        fabRedo.setOnClickListener { onRedo(); }
        fabSearch.setOnClickListener { onSearch(); closeMenu() }
        fabScreenshot.setOnClickListener { onScreenshot(); closeMenu() }
        fabEyedropper.setOnClickListener { onEyedropper(); closeMenu() }
    }

    fun setTitle(title: String) {
        txtFileName.text = title
    }

    fun renderRunState(state: RunState) {
        when (state) {
            RunState.Running -> {
                fabExecute.setImageResource(R.drawable.ic_stop)
                fabExecute.backgroundTintList = ColorStateList.valueOf("#E53935".toColorInt())
            }

            RunState.Finished -> {
                fabExecute.setImageResource(R.drawable.ic_start)
                fabExecute.backgroundTintList = ColorStateList.valueOf("#00FF19".toColorInt())
            }

            RunState.Error -> {
                fabExecute.setImageResource(R.drawable.ic_start)
                fabExecute.backgroundTintList = ColorStateList.valueOf("#F39C12".toColorInt())
            }

            RunState.Cancelled -> {
                fabExecute.setImageResource(R.drawable.ic_start)
                fabExecute.backgroundTintList = ColorStateList.valueOf("#95A5A6".toColorInt())
            }
        }
    }

    private fun openMenu() {
        menuOpened = true

        fabMenu.setImageResource(R.drawable.ic_settings_2)

        fabMenu.animate().cancel()
        fabMenu.rotation = 0f
        fabMenu.animate()
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

    private fun closeMenu() {
        menuOpened = false

        fabMenu.setImageResource(R.drawable.ic_settings)

        fabMenu.animate().cancel()
        fabMenu.rotation = 0f
        fabMenu.animate()
            .rotationBy(360f)
            .setDuration(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        miniContainer.children.forEachIndexed { index, view ->
            view.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setStartDelay(index * 40L)
                .setDuration(180)
                .withEndAction {
                    if (index == miniContainer.childCount - 1) {
                        miniContainer.visibility = View.GONE
                    }
                }
                .start()
        }
    }
}