package com.example.letslink.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslationManager(private val context: Context) {

    // Cache for translations to avoid re-translating same text
    private val translationCache = mutableMapOf<String, String>()

    // Cache for translators to reuse them
    private val translatorCache = mutableMapOf<String, Translator>()

    // Map language codes to ML Kit language codes
    private val languageMap = mapOf(
        "en" to TranslateLanguage.ENGLISH,
        "zu" to null, // Shona not supported
        "af" to TranslateLanguage.AFRIKAANS,
        "zh" to TranslateLanguage.CHINESE,
        "sn" to null // Shona not supported
    )

    /**
     * Get current language from SharedPreferences
     */
    private fun getCurrentLanguage(): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("app_language", "en") ?: "en"
    }

    /**
     * Translate text from English to the user's selected language
     */
    suspend fun translateText(
        originalText: String,
        onSuccess: (String) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val targetLang = getCurrentLanguage()

        // If English or Shona, return original text
        if (targetLang == "en" || targetLang == "sn" || targetLang == "zu") {
            onSuccess(originalText)
            return
        }

        // Check cache first
        val cacheKey = "${targetLang}_$originalText"
        if (translationCache.containsKey(cacheKey)) {
            onSuccess(translationCache[cacheKey]!!)
            return
        }

        // Get ML Kit language code
        val mlKitLangCode = languageMap[targetLang]
        if (mlKitLangCode == null) {
            Log.w("TranslationManager", "Language not supported: $targetLang")
            onSuccess(originalText)
            return
        }

        try {
            // Get or create translator
            val translator = getOrCreateTranslator(mlKitLangCode)

            // Ensure model is downloaded
            ensureModelDownloaded(translator)

            // Perform translation
            val translatedText = translator.translate(originalText).await()

            // Cache the result
            translationCache[cacheKey] = translatedText

            onSuccess(translatedText)

        } catch (e: Exception) {
            Log.e("TranslationManager", "Translation failed: ${e.message}")

            // Show toast on error
            Toast.makeText(
                context,
                "Translation unavailable, showing English",
                Toast.LENGTH_SHORT
            ).show()

            // Return original text
            onError?.invoke(originalText) ?: onSuccess(originalText)
        }
    }

    /**
     * Get existing translator or create new one
     */
    private fun getOrCreateTranslator(targetLanguage: String): Translator {
        val key = "en_$targetLanguage"

        return translatorCache.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(targetLanguage)
                .build()

            Translation.getClient(options)
        }
    }

    /**
     * Ensure translation model is downloaded
     */
    private suspend fun ensureModelDownloaded(translator: Translator) {
        val conditions = DownloadConditions.Builder()
            .requireWifi() // Only download on WiFi to save data
            .build()

        try {
            translator.downloadModelIfNeeded(conditions).await()
        } catch (e: Exception) {
            Log.e("TranslationManager", "Model download failed: ${e.message}")
            throw e
        }
    }

    /**
     * Pre-download models for a specific language
     * Call this when user changes language in settings
     */
    suspend fun preDownloadModel(languageCode: String): Boolean {
        if (languageCode == "en" || languageCode == "sn" || languageCode == "zu") {
            return true // No download needed
        }

        val mlKitLangCode = languageMap[languageCode] ?: return false

        return try {
            val translator = getOrCreateTranslator(mlKitLangCode)
            ensureModelDownloaded(translator)
            true
        } catch (e: Exception) {
            Log.e("TranslationManager", "Pre-download failed: ${e.message}")
            false
        }
    }

    /**
     * Clear translation cache (call when language changes)
     */
    fun clearCache() {
        translationCache.clear()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        translatorCache.values.forEach { it.close() }
        translatorCache.clear()
        translationCache.clear()
    }
}