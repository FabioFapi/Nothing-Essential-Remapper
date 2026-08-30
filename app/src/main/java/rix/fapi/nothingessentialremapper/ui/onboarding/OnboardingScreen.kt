package rix.fapi.nothingessentialremapper.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import rix.fapi.nothingessentialremapper.R

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineMedium)
                OnboardingPoint(
                    title = stringResource(R.string.onboarding_intro_title),
                    body = stringResource(R.string.onboarding_intro_body)
                )
                OnboardingPoint(
                    title = stringResource(R.string.onboarding_pc_title),
                    body = stringResource(R.string.onboarding_pc_body)
                )
                OnboardingPoint(
                    title = stringResource(R.string.onboarding_how_title),
                    body = stringResource(R.string.onboarding_how_body)
                )
            }
            Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.onboarding_cta))
            }
        }
    }
}

@Composable
private fun OnboardingPoint(title: String, body: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
