package rix.fapi.nothingessentialremapper.ui.mapping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import rix.fapi.nothingessentialremapper.R
import rix.fapi.nothingessentialremapper.actions.KeyAction
import rix.fapi.nothingessentialremapper.data.KeyMappingRepository
import rix.fapi.nothingessentialremapper.gesture.GestureType

@Composable
internal fun gestureLabel(gesture: GestureType): String = when (gesture) {
    GestureType.SINGLE_PRESS -> stringResource(R.string.gesture_single_press)
    GestureType.DOUBLE_PRESS -> stringResource(R.string.gesture_double_press)
    GestureType.TRIPLE_PRESS -> stringResource(R.string.gesture_triple_press)
    GestureType.LONG_PRESS -> stringResource(R.string.gesture_long_press)
}

@Composable
internal fun KeyAction.label(): String = when (this) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingScreen(
    repository: KeyMappingRepository,
    onBack: () -> Unit,
    onOpenActionPicker: (GestureType) -> Unit
) {
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
                        .clickable { onOpenActionPicker(gesture) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(gestureLabel(gesture), style = MaterialTheme.typography.titleMedium)
                        Text(action.label(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
