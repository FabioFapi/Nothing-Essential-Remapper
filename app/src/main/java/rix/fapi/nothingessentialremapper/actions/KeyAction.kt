package rix.fapi.nothingessentialremapper.actions

sealed interface KeyAction {
    data object Disabled : KeyAction
    data object ToggleFlashlight : KeyAction
    data object LockScreen : KeyAction
    data object TakeScreenshot : KeyAction
    data object GoHome : KeyAction
    data object GoBack : KeyAction
    data object OpenRecents : KeyAction
    data object OpenNotifications : KeyAction
    data object OpenQuickSettings : KeyAction
    data object MediaPlayPause : KeyAction
    data object MediaNext : KeyAction
    data object MediaPrevious : KeyAction
    data object VolumeUp : KeyAction
    data object VolumeDown : KeyAction
    data class OpenApp(val packageName: String, val label: String) : KeyAction
}

fun KeyAction.encode(): String = when (this) {
    KeyAction.Disabled -> "disabled"
    KeyAction.ToggleFlashlight -> "flashlight"
    KeyAction.LockScreen -> "lock_screen"
    KeyAction.TakeScreenshot -> "screenshot"
    KeyAction.GoHome -> "go_home"
    KeyAction.GoBack -> "go_back"
    KeyAction.OpenRecents -> "open_recents"
    KeyAction.OpenNotifications -> "open_notifications"
    KeyAction.OpenQuickSettings -> "open_quick_settings"
    KeyAction.MediaPlayPause -> "media_play_pause"
    KeyAction.MediaNext -> "media_next"
    KeyAction.MediaPrevious -> "media_previous"
    KeyAction.VolumeUp -> "volume_up"
    KeyAction.VolumeDown -> "volume_down"
    is KeyAction.OpenApp -> "open_app:$packageName:$label"
}

fun decodeKeyAction(raw: String?): KeyAction {
    if (raw.isNullOrEmpty()) return KeyAction.Disabled
    return when {
        raw == "flashlight" -> KeyAction.ToggleFlashlight
        raw == "lock_screen" -> KeyAction.LockScreen
        raw == "screenshot" -> KeyAction.TakeScreenshot
        raw == "go_home" -> KeyAction.GoHome
        raw == "go_back" -> KeyAction.GoBack
        raw == "open_recents" -> KeyAction.OpenRecents
        raw == "open_notifications" -> KeyAction.OpenNotifications
        raw == "open_quick_settings" -> KeyAction.OpenQuickSettings
        raw == "media_play_pause" -> KeyAction.MediaPlayPause
        raw == "media_next" -> KeyAction.MediaNext
        raw == "media_previous" -> KeyAction.MediaPrevious
        raw == "volume_up" -> KeyAction.VolumeUp
        raw == "volume_down" -> KeyAction.VolumeDown
        raw.startsWith("open_app:") -> {
            val parts = raw.removePrefix("open_app:").split(":", limit = 2)
            val packageName = parts.getOrNull(0).orEmpty()
            val label = parts.getOrNull(1).orEmpty()
            if (packageName.isEmpty()) KeyAction.Disabled else KeyAction.OpenApp(packageName, label)
        }
        else -> KeyAction.Disabled
    }
}
