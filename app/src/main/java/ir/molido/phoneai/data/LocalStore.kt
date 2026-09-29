package ir.molido.phoneai.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class LocalStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("molido_phone_ai", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MEMORY = "memory"
        private const val KEY_NOTES = "notes"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme"
    }

    fun memories(): List<String> = readList(KEY_MEMORY)

    fun notes(): List<String> = readList(KEY_NOTES)

    fun addMemory(value: String) = append(KEY_MEMORY, value)

    fun addNote(value: String) = append(KEY_NOTES, value)

    fun clearMemories() = prefs.edit().remove(KEY_MEMORY).apply()

    fun clearNotes() = prefs.edit().remove(KEY_NOTES).apply()

    fun language(): String = prefs.getString(KEY_LANGUAGE, "FA") ?: "FA"

    fun setLanguage(value: String) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    fun theme(): String = prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM"

    fun setTheme(value: String) = prefs.edit().putString(KEY_THEME, value).apply()

    private fun append(key: String, value: String) {
        val current = JSONArray().apply {
            readList(key).forEach { put(it) }
        }
        current.put(value.trim())
        prefs.edit().putString(key, current.toString()).apply()
    }

    private fun readList(key: String): List<String> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (i in 0 until array.length()) add(array.optString(i))
            }.filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
