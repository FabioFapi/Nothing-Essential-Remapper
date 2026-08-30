package rix.fapi.nothingessentialremapper.ui.setup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import rix.fapi.nothingessentialremapper.R
import rix.fapi.nothingessentialremapper.data.KeyMappingRepository
import rix.fapi.nothingessentialremapper.service.EssentialKeyBus

private const val ADB_DISABLE_SPACE = "adb shell pm disable-user --user 0 com.nothing.ntessentialspace"
private const val ADB_DISABLE_RECORDER = "adb shell pm disable-user --user 0 com.nothing.ntessentialrecorder"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(repository: KeyMappingRepository, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLearning by EssentialKeyBus.isLearning.collectAsState()
    val detectedScanCode by EssentialKeyBus.learnedScanCode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.setup_key_saved)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setup_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SetupStep(
                number = 1,
                title = stringResource(R.string.setup_step1_title),
                description = stringResource(R.string.setup_step1_description)
            ) {
                CommandRow(command = ADB_DISABLE_SPACE, context = context)
                CommandRow(command = ADB_DISABLE_RECORDER, context = context)
            }

            SetupStep(
                number = 2,
                title = stringResource(R.string.setup_step2_title),
                description = stringResource(R.string.setup_step2_description)
            ) {
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) {
                    Text(stringResource(R.string.setup_open_settings))
                }
            }

            SetupStep(
                number = 3,
                title = stringResource(R.string.setup_step3_title),
                description = stringResource(
                    if (isLearning) {
                        R.string.setup_step3_description_listening
                    } else {
                        R.string.setup_step3_description_idle
                    }
                )
            ) {
                Button(onClick = { EssentialKeyBus.startLearning() }, enabled = !isLearning) {
                    Text(stringResource(if (isLearning) R.string.setup_listening else R.string.setup_start_identification))
                }
                detectedScanCode?.let { code ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.setup_key_detected, code))
                    Button(onClick = {
                        scope.launch {
                            repository.setLearnedScanCode(code)
                            snackbarHostState.showSnackbar(savedMessage)
                        }
                    }) {
                        Text(stringResource(R.string.setup_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupStep(
    number: Int,
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "$number. $title", style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
            content()
        }
    }
}

@Composable
private fun CommandRow(command: String, context: Context) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = command,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("adb command", command))
        }) {
            Text(stringResource(R.string.setup_copy))
        }
    }
}
