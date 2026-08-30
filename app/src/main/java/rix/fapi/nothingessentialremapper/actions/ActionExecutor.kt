package rix.fapi.nothingessentialremapper.actions

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent

/** Executes a [KeyAction] on behalf of the accessibility service that detected the gesture. */
class ActionExecutor(private val service: AccessibilityService) {

    private val cameraManager by lazy {
        service.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private val audioManager by lazy {
        service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val torchCameraId: String? by lazy { findTorchCameraId() }
    private var flashlightOn = false

    fun execute(action: KeyAction) {
        when (action) {
            KeyAction.Disabled -> Unit
            KeyAction.ToggleFlashlight -> toggleFlashlight()
            KeyAction.LockScreen -> performGlobalActionSafely(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN, Build.VERSION_CODES.P)
            KeyAction.TakeScreenshot -> performGlobalActionSafely(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT, Build.VERSION_CODES.P)
            KeyAction.GoHome -> performGlobalActionSafely(AccessibilityService.GLOBAL_ACTION_HOME)
            KeyAction.GoBack -> performGlobalActionSafely(AccessibilityService.GLOBAL_ACTION_BACK)
            KeyAction.OpenRecents -> performGlobalActionSafely(AccessibilityService.GLOBAL_ACTION_RECENTS)
            KeyAction.OpenNotifications -> performGlobalActionSafely(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
            KeyAction.OpenQuickSettings -> performGlobalActionSafely(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
            KeyAction.MediaPlayPause -> dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            KeyAction.MediaNext -> dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
            KeyAction.MediaPrevious -> dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            KeyAction.VolumeUp -> adjustVolume(AudioManager.ADJUST_RAISE)
            KeyAction.VolumeDown -> adjustVolume(AudioManager.ADJUST_LOWER)
            is KeyAction.OpenApp -> openApp(action.packageName)
        }
    }

    private fun performGlobalActionSafely(globalAction: Int, minSdk: Int = Build.VERSION_CODES.BASE) {
        if (Build.VERSION.SDK_INT < minSdk) return
        service.performGlobalAction(globalAction)
    }

    private fun findTorchCameraId(): String? = runCatching {
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }.getOrNull()

    private fun toggleFlashlight() {
        val id = torchCameraId ?: return
        runCatching {
            flashlightOn = !flashlightOn
            cameraManager.setTorchMode(id, flashlightOn)
        }.onFailure { Log.w(TAG, "Failed to toggle flashlight", it) }
    }

    private fun dispatchMediaKey(keyCode: Int) {
        val eventTime = SystemClock.uptimeMillis()
        runCatching {
            audioManager.dispatchMediaKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0))
            audioManager.dispatchMediaKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0))
        }.onFailure { Log.w(TAG, "Failed to dispatch media key $keyCode", it) }
    }

    private fun adjustVolume(direction: Int) {
        runCatching {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        }.onFailure { Log.w(TAG, "Failed to adjust volume", it) }
    }

    private fun openApp(packageName: String) {
        val launchIntent = service.packageManager.getLaunchIntentForPackage(packageName) ?: return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { service.startActivity(launchIntent) }
            .onFailure { Log.w(TAG, "Failed to open app $packageName", it) }
    }

    private companion object {
        const val TAG = "ActionExecutor"
    }
}
