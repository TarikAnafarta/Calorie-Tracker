package com.tarik.calorietracker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val NAME_KEY = stringPreferencesKey("name")
        val AGE_KEY = intPreferencesKey("age")
        val WEIGHT_KEY = floatPreferencesKey("weight")
        val HEIGHT_KEY = intPreferencesKey("height")
        val IS_MALE_KEY = booleanPreferencesKey("is_male")
        val IS_DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")
    }

    val userData: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[NAME_KEY] ?: "-",
            age = prefs[AGE_KEY] ?: 0,
            weight = prefs[WEIGHT_KEY] ?: 0f,
            height = prefs[HEIGHT_KEY] ?: 0,
            isMale = prefs[IS_MALE_KEY] ?: true,
            isDarkTheme = prefs[IS_DARK_THEME_KEY] ?: false
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[NAME_KEY] = profile.name
            prefs[AGE_KEY] = profile.age
            prefs[WEIGHT_KEY] = profile.weight
            prefs[HEIGHT_KEY] = profile.height
            prefs[IS_MALE_KEY] = profile.isMale
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_DARK_THEME_KEY] = isDark
        }
    }
}

data class UserProfile(
    val name: String,
    val age: Int,
    val weight: Float,
    val height: Int,
    val isMale: Boolean,
    val isDarkTheme: Boolean
)