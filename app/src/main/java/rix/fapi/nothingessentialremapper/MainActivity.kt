package rix.fapi.nothingessentialremapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import rix.fapi.nothingessentialremapper.data.KeyMappingRepository
import rix.fapi.nothingessentialremapper.ui.navigation.AppNavHost
import rix.fapi.nothingessentialremapper.ui.theme.NothingEssentialRemapperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = KeyMappingRepository(applicationContext)
        setContent {
            NothingEssentialRemapperTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(repository = repository)
                }
            }
        }
    }
}
