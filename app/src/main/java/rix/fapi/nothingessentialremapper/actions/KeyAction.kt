package rix.fapi.nothingessentialremapper.actions

sealed interface KeyAction {
    data object Disabled : KeyAction
    data object ToggleFlashlight : KeyAction
    data object LockScreen : KeyAction
    data object TakeScreenshot : KeyAction
    data class OpenApp(val packageName: String, val label: String) : KeyAction
}

fun KeyAction.encode(): String = when (this) {
    KeyAction.Disabled -> "disabled"
    KeyAction.ToggleFlashlight -> "flashlight"
    KeyAction.LockScreen -> "lock_screen"
    KeyAction.TakeScreenshot -> "screenshot"
    is KeyAction.OpenApp -> "open_app:$packageName:$label"
}

fun decodeKeyAction(raw: String?): KeyAction {
    if (raw.isNullOrEmpty()) return KeyAction.Disabled
    return when {
        raw == "flashlight" -> KeyAction.ToggleFlashlight
        raw == "lock_screen" -> KeyAction.LockScreen
        raw == "screenshot" -> KeyAction.TakeScreenshot
        raw.startsWith("open_app:") -> {
            val parts = raw.removePrefix("open_app:").split(":", limit = 2)
            val packageName = parts.getOrNull(0).orEmpty()
            val label = parts.getOrNull(1).orEmpty()
            if (packageName.isEmpty()) KeyAction.Disabled else KeyAction.OpenApp(packageName, label)
        }
        else -> KeyAction.Disabled
    }
}
