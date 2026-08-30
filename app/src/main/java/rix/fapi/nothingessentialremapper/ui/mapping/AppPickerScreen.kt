package rix.fapi.nothingessentialremapper.ui.mapping

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import rix.fapi.nothingessentialremapper.R
import rix.fapi.nothingessentialremapper.actions.KeyAction
import rix.fapi.nothingessentialremapper.data.KeyMappingRepository
import rix.fapi.nothingessentialremapper.gesture.GestureType

private data class AppEntry(val packageName: String, val label: String)

private val ALPHABET_RAIL = ('A'..'Z').map { it.toString() } + "#"

private fun indexKeyFor(label: String): String {
    val first = label.trim().firstOrNull()?.uppercaseChar()
    return if (first != null && first in 'A'..'Z') first.toString() else "#"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    repository: KeyMappingRepository,
    gesture: GestureType,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allApps = remember { loadAllApps(context) }
    var query by remember { mutableStateOf("") }
    val filteredApps = remember(allApps, query) {
        if (query.isBlank()) allApps else allApps.filter { it.label.contains(query, ignoreCase = true) }
    }
    val listState = rememberLazyListState()
    val firstIndexForLetter = remember(filteredApps) {
        buildMap {
            filteredApps.forEachIndexed { index, app ->
                val key = indexKeyFor(app.label)
                if (key !in this) put(key, index)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_picker_title)) },
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
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.app_picker_search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.app_picker_clear_search))
                        }
                    }
                },
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        ListItem(
                            headlineContent = { Text(app.label) },
                            modifier = Modifier.clickable {
                                scope.launch {
                                    repository.setMapping(gesture, KeyAction.OpenApp(app.packageName, app.label))
                                    onBack()
                                }
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                AlphabetRail(
                    availableLetters = firstIndexForLetter.keys,
                    onLetterSelected = { letter ->
                        firstIndexForLetter[letter]?.let { index ->
                            scope.launch { listState.animateScrollToItem(index) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(28.dp)
                )
            }
        }
    }
}

@Composable
private fun AlphabetRail(
    availableLetters: Set<String>,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ALPHABET_RAIL.forEach { letter ->
                val isAvailable = letter in availableLetters
                Text(
                    text = letter,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    color = if (isAvailable) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable(enabled = isAvailable) { onLetterSelected(letter) }
                )
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun loadAllApps(context: Context): List<AppEntry> {
    val packageManager = context.packageManager
    return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { it.packageName != context.packageName }
        .map { appInfo ->
            AppEntry(
                packageName = appInfo.packageName,
                label = appInfo.loadLabel(packageManager).toString()
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}
