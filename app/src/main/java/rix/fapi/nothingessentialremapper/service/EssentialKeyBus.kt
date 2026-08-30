package rix.fapi.nothingessentialremapper.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-process channel between the UI and [EssentialKeyAccessibilityService]. The service always
 * runs in the app's own process, so a plain singleton is enough — no IPC required.
 */
object EssentialKeyBus {
    private val _isLearning = MutableStateFlow(false)
    val isLearning = _isLearning.asStateFlow()

    private val _learnedScanCode = MutableStateFlow<Int?>(null)
    val learnedScanCode = _learnedScanCode.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning = _isServiceRunning.asStateFlow()

    /** Called from the UI to enter "press the Essential Key now" mode. */
    fun startLearning() {
        _learnedScanCode.value = null
        _isLearning.value = true
    }

    fun reportServiceState(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun reportDetectedScanCode(scanCode: Int) {
        _learnedScanCode.value = scanCode
        _isLearning.value = false
    }
}
