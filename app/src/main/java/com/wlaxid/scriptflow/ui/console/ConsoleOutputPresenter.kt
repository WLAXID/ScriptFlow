package com.wlaxid.scriptflow.ui.console

import com.wlaxid.scriptflow.runtime.RunState

class ConsoleOutputPresenter(
    private val consoleController: ConsoleController
) {

    fun onStateChanged(state: RunState) {
        when (state) {
            RunState.Running -> {
                consoleController.clear()
                consoleController.appendMessage(
                    "=== Running ===",
                    ConsoleMessageType.SYSTEM
                )
            }

            RunState.Finished -> {
                consoleController.appendMessage(
                    "\n=== Finished ===",
                    ConsoleMessageType.SYSTEM
                )
            }

            RunState.Error -> {
                consoleController.appendMessage(
                    "\n=== Finished with errors ===",
                    ConsoleMessageType.SYSTEM
                )
            }

            RunState.Cancelled -> {
                consoleController.appendMessage(
                    "\n=== Cancelled ===",
                    ConsoleMessageType.SYSTEM
                )
            }
        }
    }

    fun onOutput(text: String) {
        consoleController.appendFragment(text, ConsoleMessageType.OUTPUT)
    }

    fun onError(text: String) {
        consoleController.appendFragment(text, ConsoleMessageType.ERROR)
    }
}
