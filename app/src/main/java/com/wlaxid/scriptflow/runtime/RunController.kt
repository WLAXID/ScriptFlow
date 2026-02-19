package com.wlaxid.scriptflow.runtime

class RunController {

    private var isRunning = false

    fun toggle(): RunState {
        isRunning = !isRunning
        return currentState()
    }

    fun currentState(): RunState {
        return if (isRunning) {
            RunState.Running
        } else {
            RunState.Stopped
        }
    }
}

sealed class RunState {
    object Running : RunState()
    object Stopped : RunState()
}
