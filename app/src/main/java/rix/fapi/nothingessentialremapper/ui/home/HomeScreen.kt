package rix.fapi.nothingessentialremapper.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import rix.fapi.nothingessentialremapper.R
import rix.fapi.nothingessentialremapper.data.KeyMappingRepository
import rix.fapi.nothingessentialremapper.service.isEssentialKeyServiceEnabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: KeyMappingRepository,
    onOpenSetup: () -> Unit,
    onOpenMapping: () -> Unit
) {
    val context = LocalContext.current
    val learnedScanCode by repository.learnedScanCode.collectAsState(initial = null)
    var accessibilityEnabled by remember { mutableStateOf(isEssentialKeyServiceEnabled(context)) }

    LaunchedEffect(Unit) {
        accessibilityEnabled = isEssentialKeyServiceEnabled(context)
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                title = stringResource(R.string.home_status_accessibility_title),
                status = stringResource(
                    if (accessibilityEnabled) R.string.home_status_active else R.string.home_status_inactive
                )
            )
            StatusCard(
                title = stringResource(R.string.home_status_key_title),
                status = learnedScanCode?.let { stringResource(R.string.home_status_key_identified, it) }
                    ?: stringResource(R.string.home_status_key_not_identified)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onOpenSetup, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.home_button_setup))
            }
            Button(
                onClick = onOpenMapping,
                modifier = Modifier.fillMaxWidth(),
                enabled = accessibilityEnabled && learnedScanCode != null
            ) {
                Text(stringResource(R.string.home_button_mapping))
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, status: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = status, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
