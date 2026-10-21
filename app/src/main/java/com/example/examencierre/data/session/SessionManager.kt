package com.example.examencierre.data.session

import android.content.Context
import androidx.core.content.edit

/**
 * Gestiona la sesión del usuario en curso (id y datos básicos) usando SharedPreferences.
 */
class SessionManager(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var userId: Long
        get() = preferences.getLong(KEY_USER_ID, NO_USER)
        set(value) = preferences.edit { putLong(KEY_USER_ID, value) }

    var userName: String
        get() = preferences.getString(KEY_USER_NAME, "").orEmpty()
        set(value) = preferences.edit { putString(KEY_USER_NAME, value) }

    var userLastName: String
        get() = preferences.getString(KEY_USER_LAST_NAME, "").orEmpty()
        set(value) = preferences.edit { putString(KEY_USER_LAST_NAME, value) }

    var userEmail: String
        get() = preferences.getString(KEY_USER_EMAIL, "").orEmpty()
        set(value) = preferences.edit { putString(KEY_USER_EMAIL, value) }

    val isLoggedIn: Boolean get() = userId != NO_USER

    fun saveSession(userId: Long, nombre: String, apellidos: String, correo: String) {
        preferences.edit {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, nombre)
            putString(KEY_USER_LAST_NAME, apellidos)
            putString(KEY_USER_EMAIL, correo)
        }
    }

    fun clearSession() {
        preferences.edit { clear() }
    }

    private companion object {
        const val PREFERENCES_NAME = "examencierre_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_LAST_NAME = "user_last_name"
        const val KEY_USER_EMAIL = "user_email"
        const val NO_USER = -1L
    }
}
