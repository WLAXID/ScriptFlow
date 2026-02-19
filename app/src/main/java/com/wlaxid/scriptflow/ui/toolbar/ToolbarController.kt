package com.wlaxid.scriptflow.ui.toolbar

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import com.wlaxid.scriptflow.R

class ToolbarController(
    root: View,
    private val drawerLayout: DrawerLayout,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onSearch: () -> Unit = {},
    onScreenshot: () -> Unit = {},
    onEyedropper: () -> Unit = {}
) {

    private val btnOptions: ImageView = root.findViewById(R.id.btnOptions)
    private val txtFileName: TextView = root.findViewById(R.id.txtFileName)

    // заглушки
    private val btnUndo: ImageView = root.findViewById(R.id.btnUndo)
    private val btnRedo: ImageView = root.findViewById(R.id.btnRedo)
    private val btnSearch: ImageView = root.findViewById(R.id.btnSearch)
    private val btnScreenshot: ImageView = root.findViewById(R.id.btnScreenshot)
    private val btnEyedropper: ImageView = root.findViewById(R.id.btnEyedropper)

    init {
        btnOptions.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // пока заглушки — просто кликабельные
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
