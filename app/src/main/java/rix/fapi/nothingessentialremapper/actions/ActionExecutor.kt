package rix.fapi.nothingessentialremapper.actions

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log

/** Executes a [KeyAction] on behalf of the accessibility service that detected the gesture. */
class ActionExecutor(private val service: AccessibilityService) {

    private val cameraManager by lazy {
        service.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private val torchCameraId: String? by lazy { findTorchCameraId() }
    private var flashlightOn = false

    fun execute(action: KeyAction) {
        when (action) {
            KeyAction.Disabled -> Unit
            KeyAction.ToggleFlashlight -> toggleFlashlight()
            KeyAction.LockScreen -> performGlobalActionSafely(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
            KeyAction.TakeScreenshot -> performGlobalActionSafely(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
            is KeyAction.OpenApp -> openApp(action.packageName)
        }
    }

    private fun performGlobalActionSafely(globalAction: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
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
