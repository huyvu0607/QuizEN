package com.lumina.app.data.source.local.pref

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "lumina_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_FIREBASE_UID = "firebase_uid"
    }

    fun saveSession(userId: Long, email: String, firebaseUid: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_FIREBASE_UID, firebaseUid)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun getFirebaseUid(): String? = prefs.getString(KEY_FIREBASE_UID, null)

    fun isUnitGrouped(unitId: Long): Boolean {
        return prefs.getBoolean("unit_grouped_$unitId", false)
    }

    fun setUnitGrouped(unitId: Long, grouped: Boolean) {
        prefs.edit().putBoolean("unit_grouped_$unitId", grouped).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}