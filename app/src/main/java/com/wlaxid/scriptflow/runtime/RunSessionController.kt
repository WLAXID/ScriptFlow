package com.wlaxid.scriptflow.runtime

import android.content.Context
import com.wlaxid.scriptflow.ui.console.ConsoleOutputPresenter

class RunSessionController(
    context: Context,
    private val consolePresenter: ConsoleOutputPresenter,
    private val onUiStateChanged: (RunState) -> Unit
) {

    private val runController = RunController(
        context = context,
        onStateChanged = { state ->
            onUiStateChanged(state)
            consolePresenter.onStateChanged(state)
        },
        onOutput = { text ->
            consolePresenter.onOutput(text)
        },
        onError = { text ->
            consolePresenter.onError(text)
        }
    )

    fun execute(code: String) {
        runController.execute(code)
    }

    fun stop() {
        runController.stop()
    }

    fun currentState(): RunState =
        runController.currentState()

    fun destroy() {
        runController.destroy()
    }
}

