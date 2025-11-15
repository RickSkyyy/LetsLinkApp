package com.example.letslink.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    /**
     * Apply the saved locale to the context
     */
    fun setLocale(context: Context): Context {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"
        return updateResources(context, languageCode)
    }

    /**
     * Update the context with the new locale
     */
    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }

    /**
     * Get the current language code
     */
    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("app_language", "en") ?: "en"
    }
}