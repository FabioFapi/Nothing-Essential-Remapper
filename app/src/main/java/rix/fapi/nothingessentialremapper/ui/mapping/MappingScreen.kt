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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
    is KeyAction.OpenApp -> stringResource(R.string.action_open_app, label)
}

private enum class ActionOption { DISABLED, FLASHLIGHT, LOCK_SCREEN, SCREENSHOT, OPEN_APP }

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
                when (option) {
                    ActionOption.DISABLED -> scope.launch { repository.setMapping(gesture, KeyAction.Disabled) }
                    ActionOption.FLASHLIGHT -> scope.launch { repository.setMapping(gesture, KeyAction.ToggleFlashlight) }
                    ActionOption.LOCK_SCREEN -> scope.launch { repository.setMapping(gesture, KeyAction.LockScreen) }
                    ActionOption.SCREENSHOT -> scope.launch { repository.setMapping(gesture, KeyAction.TakeScreenshot) }
                    ActionOption.OPEN_APP -> onPickApp(gesture)
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
            Column {
                DialogOption(stringResource(R.string.action_none)) { onSelect(ActionOption.DISABLED) }
                DialogOption(stringResource(R.string.action_flashlight)) { onSelect(ActionOption.FLASHLIGHT) }
                DialogOption(stringResource(R.string.action_lock_screen)) { onSelect(ActionOption.LOCK_SCREEN) }
                DialogOption(stringResource(R.string.action_screenshot)) { onSelect(ActionOption.SCREENSHOT) }
                DialogOption(stringResource(R.string.action_open_app_picker)) { onSelect(ActionOption.OPEN_APP) }
            }
        }
    )
}

@Composable
private fun DialogOption(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text)
    }
}
