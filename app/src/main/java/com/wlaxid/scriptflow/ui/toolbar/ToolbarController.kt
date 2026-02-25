package com.wlaxid.scriptflow.ui.toolbar

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wlaxid.scriptflow.R

class ToolbarController(
    root: View,
    fabRoot: View,
    private val drawerLayout: DrawerLayout,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onSearch: () -> Unit = {},
    onScreenshot: () -> Unit = {},
    onEyedropper: () -> Unit = {}
) {

    private val btnOptions: ImageView = root.findViewById(R.id.btnOptions)
    private val txtFileName: TextView = root.findViewById(R.id.txtFileName)

    private val btnUndo: FloatingActionButton =
        fabRoot.findViewById(R.id.fabUndo)

    private val btnRedo: FloatingActionButton =
        fabRoot.findViewById(R.id.fabRedo)

    private val btnSearch: FloatingActionButton =
        fabRoot.findViewById(R.id.fabSearch)

    private val btnScreenshot: FloatingActionButton =
        fabRoot.findViewById(R.id.fabScreenshot)

    private val btnEyedropper: FloatingActionButton =
        fabRoot.findViewById(R.id.fabEyedropper)

    init {
        btnOptions.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        btnUndo.setOnClickListener { onUndo() }
        btnRedo.setOnClickListener { onRedo() }
        btnSearch.setOnClickListener { onSearch() }
        btnScreenshot.setOnClickListener { onScreenshot() }
        btnEyedropper.setOnClickListener { onEyedropper() }
    }

    fun setTitle(title: String) {
        txtFileName.text = title
    }
}