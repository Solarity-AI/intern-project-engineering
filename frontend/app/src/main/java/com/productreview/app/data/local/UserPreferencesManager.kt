package com.productreview.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val SORT_PREFERENCE_KEY = stringPreferencesKey("sort_preference")
        private val GRID_MODE_KEY = intPreferencesKey("grid_mode")
        private val SEARCH_HISTORY_KEY = stringSetPreferencesKey("search_history")
    }

    // ==================== USER ID ====================
    
    suspend fun getUserId(): String {
        val preferences = dataStore.data.first()
        return preferences[USER_ID_KEY] ?: run {
            val newUserId = UUID.randomUUID().toString()
            dataStore.edit { it[USER_ID_KEY] = newUserId }
            newUserId
        }
    }

    // ==================== THEME ====================
    
    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map { preferences ->
        when (preferences[THEME_MODE_KEY]) {
            "dark" -> ThemeMode.DARK
            "light" -> ThemeMode.LIGHT
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = when (mode) {
                ThemeMode.DARK -> "dark"
                ThemeMode.LIGHT -> "light"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }

    // ==================== SORT PREFERENCE ====================
    
    val sortPreferenceFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[SORT_PREFERENCE_KEY] ?: "name,asc"
    }

    suspend fun setSortPreference(sort: String) {
        dataStore.edit { preferences ->
            preferences[SORT_PREFERENCE_KEY] = sort
        }
    }

    // ==================== GRID MODE ====================
    
    val gridModeFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[GRID_MODE_KEY] ?: 2
    }

    suspend fun setGridMode(columns: Int) {
        dataStore.edit { preferences ->
            preferences[GRID_MODE_KEY] = columns
        }
    }

    // ==================== SEARCH HISTORY ====================
    
    val searchHistoryFlow: Flow<List<String>> = dataStore.data.map { preferences ->
        preferences[SEARCH_HISTORY_KEY]?.toList() ?: emptyList()
    }

    suspend fun addSearchTerm(term: String) {
        if (term.isBlank()) return
        dataStore.edit { preferences ->
            val current = preferences[SEARCH_HISTORY_KEY]?.toMutableSet() ?: mutableSetOf()
            // Remove if exists (to move to top)
            current.remove(term)
            // Keep only last 5
            val newHistory = (listOf(term) + current.toList()).take(5).toSet()
            preferences[SEARCH_HISTORY_KEY] = newHistory
        }
    }

    suspend fun removeSearchTerm(term: String) {
        dataStore.edit { preferences ->
            val current = preferences[SEARCH_HISTORY_KEY]?.toMutableSet() ?: mutableSetOf()
            current.remove(term)
            preferences[SEARCH_HISTORY_KEY] = current
        }
    }

    suspend fun clearSearchHistory() {
        dataStore.edit { preferences ->
            preferences.remove(SEARCH_HISTORY_KEY)
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
