package rix.fapi.nothingessentialremapper.service

import android.content.Context
import android.provider.Settings

fun isEssentialKeyServiceEnabled(context: Context): Boolean {
    val expectedComponent = "${context.packageName}/${EssentialKeyAccessibilityService::class.java.name}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
}
