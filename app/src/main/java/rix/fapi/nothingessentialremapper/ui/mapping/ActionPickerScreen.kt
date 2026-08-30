package rix.fapi.nothingessentialremapper.ui.mapping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import rix.fapi.nothingessentialremapper.R
import rix.fapi.nothingessentialremapper.actions.KeyAction
import rix.fapi.nothingessentialremapper.data.KeyMappingRepository
import rix.fapi.nothingessentialremapper.gesture.GestureType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPickerScreen(
    repository: KeyMappingRepository,
    gesture: GestureType,
    onBack: () -> Unit,
    onPickApp: () -> Unit
) {
    val scope = rememberCoroutineScope()

    fun select(action: KeyAction) {
        scope.launch { repository.setMapping(gesture, action) }
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gestureLabel(gesture)) },
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
                .fillMaxSize()
        ) {
            item { ActionRow(stringResource(R.string.action_none)) { select(KeyAction.Disabled) } }
            item { ActionRow(stringResource(R.string.action_open_app_picker), onClick = onPickApp) }

            item { SectionHeader(stringResource(R.string.mapping_section_system)) }
            item { ActionRow(stringResource(R.string.action_go_home)) { select(KeyAction.GoHome) } }
            item { ActionRow(stringResource(R.string.action_key_back)) { select(KeyAction.GoBack) } }
            item { ActionRow(stringResource(R.string.action_open_recents)) { select(KeyAction.OpenRecents) } }
            item { ActionRow(stringResource(R.string.action_open_notifications)) { select(KeyAction.OpenNotifications) } }
            item { ActionRow(stringResource(R.string.action_open_quick_settings)) { select(KeyAction.OpenQuickSettings) } }
            item { ActionRow(stringResource(R.string.action_lock_screen)) { select(KeyAction.LockScreen) } }
            item { ActionRow(stringResource(R.string.action_screenshot)) { select(KeyAction.TakeScreenshot) } }
            item { ActionRow(stringResource(R.string.action_flashlight)) { select(KeyAction.ToggleFlashlight) } }

            item { SectionHeader(stringResource(R.string.mapping_section_media)) }
            item { ActionRow(stringResource(R.string.action_media_play_pause)) { select(KeyAction.MediaPlayPause) } }
            item { ActionRow(stringResource(R.string.action_media_next)) { select(KeyAction.MediaNext) } }
            item { ActionRow(stringResource(R.string.action_media_previous)) { select(KeyAction.MediaPrevious) } }
            item { ActionRow(stringResource(R.string.action_volume_up)) { select(KeyAction.VolumeUp) } }
            item { ActionRow(stringResource(R.string.action_volume_down)) { select(KeyAction.VolumeDown) } }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ActionRow(text: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text) },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}
