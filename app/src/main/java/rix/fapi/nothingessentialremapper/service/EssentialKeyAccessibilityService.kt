package rix.fapi.nothingessentialremapper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import rix.fapi.nothingessentialremapper.actions.ActionExecutor
import rix.fapi.nothingessentialremapper.data.KeyMappingRepository
import rix.fapi.nothingessentialremapper.gesture.GestureClassifier
import rix.fapi.nothingessentialremapper.gesture.GestureType

/**
 * Captures Essential Key presses. The stock Nothing OS handling for that key is expected to
 * already be disabled at the package level (see the setup screen) so this service owns every
 * gesture, including a single tap, without competing with the system.
 *
 * The Essential Key arrives in the input pipeline as keyCode=0 with an OEM-specific scan code
 * (observed as 250 on Phone (3)), so it isn't mapped by any public key layout. The scan code is
 * therefore learned once via [EssentialKeyBus] and persisted through [KeyMappingRepository].
 */
class EssentialKeyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private val classifier = GestureClassifier()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var repository: KeyMappingRepository
    private lateinit var actionExecutor: ActionExecutor

    private var learnedScanCode: Int? = null
    private var timeoutRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo?.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        repository = KeyMappingRepository(applicationContext)
        actionExecutor = ActionExecutor(this)
        serviceScope.launch {
            repository.learnedScanCode.collect { learnedScanCode = it }
        }
        EssentialKeyBus.reportServiceState(true)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (EssentialKeyBus.isLearning.value) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                EssentialKeyBus.reportDetectedScanCode(event.scanCode)
            }
            return true
        }

        val currentLearned = learnedScanCode ?: return false
        if (event.scanCode != currentLearned) return false

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount == 0) {
                    cancelPendingTimeout()
                    classifier.onKeyDown(event.eventTime)
                }
            }
            KeyEvent.ACTION_UP -> {
                val gesture = classifier.onKeyUp(event.eventTime)
                if (gesture != null) {
                    dispatch(gesture)
                } else {
                    scheduleMultiPressTimeout()
                }
            }
        }
        return true
    }

    private fun scheduleMultiPressTimeout() {
        cancelPendingTimeout()
        val runnable = Runnable {
            classifier.onMultiPressTimeout()?.let { dispatch(it) }
        }
        timeoutRunnable = runnable
        handler.postDelayed(runnable, classifier.multiPressTimeoutMillis)
    }

    private fun cancelPendingTimeout() {
        timeoutRunnable?.let(handler::removeCallbacks)
        timeoutRunnable = null
    }

    private fun dispatch(gesture: GestureType) {
        serviceScope.launch {
            actionExecutor.execute(repository.snapshotMapping(gesture))
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        cancelPendingTimeout()
        EssentialKeyBus.reportServiceState(false)
        serviceScope.cancel()
    }
}
