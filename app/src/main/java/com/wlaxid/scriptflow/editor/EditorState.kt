package com.wlaxid.scriptflow.editor

import android.net.Uri

class EditorState(defaultFileName: String) {

    var currentFileUri: Uri? = null
        private set

    var currentFileName: String = defaultFileName
        private set

    var isDirty: Boolean = false
        private set

    private var initialized = false

    fun onInitialTextSet() {
        initialized = true
        isDirty = false
    }

    fun onFileOpened(uri: Uri, name: String) {
        currentFileUri = uri
        currentFileName = name
        isDirty = false
    }

    fun onFileSaved(uri: Uri, name: String) {
        currentFileUri = uri
        currentFileName = name
        isDirty = false
    }

    fun onTextChanged() {
        isDirty = true
    }

    fun displayName(): String {
        return if (isDirty) "$currentFileName *" else currentFileName
    }
}

