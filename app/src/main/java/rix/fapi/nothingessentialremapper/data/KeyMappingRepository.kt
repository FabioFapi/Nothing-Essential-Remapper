package rix.fapi.nothingessentialremapper.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import rix.fapi.nothingessentialremapper.actions.KeyAction
import rix.fapi.nothingessentialremapper.actions.decodeKeyAction
import rix.fapi.nothingessentialremapper.actions.encode
import rix.fapi.nothingessentialremapper.gesture.GestureType

private val Context.dataStore by preferencesDataStore(name = "essential_key_settings")

/** Persists the learned Essential Key scan code and the per-gesture action mapping. */
class KeyMappingRepository(private val context: Context) {

    private val scanCodeKey = intPreferencesKey("learned_scan_code")
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private fun mappingKey(gesture: GestureType) = stringPreferencesKey("mapping_${gesture.name}")

    val learnedScanCode: Flow<Int?> = context.dataStore.data.map { prefs -> prefs[scanCodeKey] }

    val isOnboardingCompleted: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[onboardingCompletedKey] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[onboardingCompletedKey] = completed }
    }

    suspend fun setLearnedScanCode(scanCode: Int) {
        context.dataStore.edit { it[scanCodeKey] = scanCode }
    }

    fun mapping(gesture: GestureType): Flow<KeyAction> =
        context.dataStore.data.map { prefs -> decodeKeyAction(prefs[mappingKey(gesture)]) }

    suspend fun setMapping(gesture: GestureType, action: KeyAction) {
        context.dataStore.edit { it[mappingKey(gesture)] = action.encode() }
    }

    suspend fun snapshotMapping(gesture: GestureType): KeyAction = mapping(gesture).first()
}
