package com.wlaxid.scriptflow.editor

import android.net.Uri

class EditorState {

    var currentFileUri: Uri? = null
        private set

    var currentFileName: String = "script.py"
        private set

    var isDirty: Boolean = false
        private set

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

