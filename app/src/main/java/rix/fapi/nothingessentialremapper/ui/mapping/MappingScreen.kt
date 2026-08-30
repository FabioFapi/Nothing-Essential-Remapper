package rix.fapi.nothingessentialremapper.ui.mapping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import rix.fapi.nothingessentialremapper.R
import rix.fapi.nothingessentialremapper.actions.KeyAction
import rix.fapi.nothingessentialremapper.data.KeyMappingRepository
import rix.fapi.nothingessentialremapper.gesture.GestureType

@Composable
private fun gestureLabel(gesture: GestureType): String = when (gesture) {
    GestureType.SINGLE_PRESS -> stringResource(R.string.gesture_single_press)
    GestureType.DOUBLE_PRESS -> stringResource(R.string.gesture_double_press)
    GestureType.TRIPLE_PRESS -> stringResource(R.string.gesture_triple_press)
    GestureType.LONG_PRESS -> stringResource(R.string.gesture_long_press)
}

@Composable
private fun KeyAction.label(): String = when (this) {
    KeyAction.Disabled -> stringResource(R.string.action_none)
    KeyAction.ToggleFlashlight -> stringResource(R.string.action_flashlight)
    KeyAction.LockScreen -> stringResource(R.string.action_lock_screen)
    KeyAction.TakeScreenshot -> stringResource(R.string.action_screenshot)
    KeyAction.GoHome -> stringResource(R.string.action_go_home)
    KeyAction.GoBack -> stringResource(R.string.action_key_back)
    KeyAction.OpenRecents -> stringResource(R.string.action_open_recents)
    KeyAction.OpenNotifications -> stringResource(R.string.action_open_notifications)
    KeyAction.OpenQuickSettings -> stringResource(R.string.action_open_quick_settings)
    KeyAction.MediaPlayPause -> stringResource(R.string.action_media_play_pause)
    KeyAction.MediaNext -> stringResource(R.string.action_media_next)
    KeyAction.MediaPrevious -> stringResource(R.string.action_media_previous)
    KeyAction.VolumeUp -> stringResource(R.string.action_volume_up)
    KeyAction.VolumeDown -> stringResource(R.string.action_volume_down)
    is KeyAction.OpenApp -> stringResource(R.string.action_open_app, label)
}

private enum class ActionOption {
    DISABLED, OPEN_APP,
    GO_HOME, GO_BACK, OPEN_RECENTS, OPEN_NOTIFICATIONS, OPEN_QUICK_SETTINGS, LOCK_SCREEN, SCREENSHOT, FLASHLIGHT,
    MEDIA_PLAY_PAUSE, MEDIA_NEXT, MEDIA_PREVIOUS, VOLUME_UP, VOLUME_DOWN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingScreen(
    repository: KeyMappingRepository,
    onBack: () -> Unit,
    onPickApp: (GestureType) -> Unit
) {
    val scope = rememberCoroutineScope()
    var gestureForDialog by remember { mutableStateOf<GestureType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mapping_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(GestureType.entries.toList()) { gesture ->
                val action by repository.mapping(gesture).collectAsState(initial = KeyAction.Disabled)
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { gestureForDialog = gesture }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(gestureLabel(gesture), style = MaterialTheme.typography.titleMedium)
                        Text(action.label(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    gestureForDialog?.let { gesture ->
        ActionPickerDialog(
            onDismiss = { gestureForDialog = null },
            onSelect = { option ->
                gestureForDialog = null
                val newAction = when (option) {
                    ActionOption.DISABLED -> KeyAction.Disabled
                    ActionOption.OPEN_APP -> null
                    ActionOption.GO_HOME -> KeyAction.GoHome
                    ActionOption.GO_BACK -> KeyAction.GoBack
                    ActionOption.OPEN_RECENTS -> KeyAction.OpenRecents
                    ActionOption.OPEN_NOTIFICATIONS -> KeyAction.OpenNotifications
                    ActionOption.OPEN_QUICK_SETTINGS -> KeyAction.OpenQuickSettings
                    ActionOption.LOCK_SCREEN -> KeyAction.LockScreen
                    ActionOption.SCREENSHOT -> KeyAction.TakeScreenshot
                    ActionOption.FLASHLIGHT -> KeyAction.ToggleFlashlight
                    ActionOption.MEDIA_PLAY_PAUSE -> KeyAction.MediaPlayPause
                    ActionOption.MEDIA_NEXT -> KeyAction.MediaNext
                    ActionOption.MEDIA_PREVIOUS -> KeyAction.MediaPrevious
                    ActionOption.VOLUME_UP -> KeyAction.VolumeUp
                    ActionOption.VOLUME_DOWN -> KeyAction.VolumeDown
                }
                if (newAction != null) {
                    scope.launch { repository.setMapping(gesture, newAction) }
                } else {
                    onPickApp(gesture)
                }
            }
        )
    }
}

@Composable
private fun ActionPickerDialog(onDismiss: () -> Unit, onSelect: (ActionOption) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mapping_dialog_title)) },
        confirmButton = {},
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DialogOption(stringResource(R.string.action_none)) { onSelect(ActionOption.DISABLED) }
                DialogOption(stringResource(R.string.action_open_app_picker)) { onSelect(ActionOption.OPEN_APP) }

                SectionLabel(stringResource(R.string.mapping_section_system))
                DialogOption(stringResource(R.string.action_go_home)) { onSelect(ActionOption.GO_HOME) }
                DialogOption(stringResource(R.string.action_key_back)) { onSelect(ActionOption.GO_BACK) }
                DialogOption(stringResource(R.string.action_open_recents)) { onSelect(ActionOption.OPEN_RECENTS) }
                DialogOption(stringResource(R.string.action_open_notifications)) { onSelect(ActionOption.OPEN_NOTIFICATIONS) }
                DialogOption(stringResource(R.string.action_open_quick_settings)) { onSelect(ActionOption.OPEN_QUICK_SETTINGS) }
                DialogOption(stringResource(R.string.action_lock_screen)) { onSelect(ActionOption.LOCK_SCREEN) }
                DialogOption(stringResource(R.string.action_screenshot)) { onSelect(ActionOption.SCREENSHOT) }
                DialogOption(stringResource(R.string.action_flashlight)) { onSelect(ActionOption.FLASHLIGHT) }

                SectionLabel(stringResource(R.string.mapping_section_media))
                DialogOption(stringResource(R.string.action_media_play_pause)) { onSelect(ActionOption.MEDIA_PLAY_PAUSE) }
                DialogOption(stringResource(R.string.action_media_next)) { onSelect(ActionOption.MEDIA_NEXT) }
                DialogOption(stringResource(R.string.action_media_previous)) { onSelect(ActionOption.MEDIA_PREVIOUS) }
                DialogOption(stringResource(R.string.action_volume_up)) { onSelect(ActionOption.VOLUME_UP) }
                DialogOption(stringResource(R.string.action_volume_down)) { onSelect(ActionOption.VOLUME_DOWN) }
            }
        }
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    HorizontalDivider()
}

@Composable
private fun DialogOption(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text)
    }
}
