package com.spa.management.data.api

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "spa_session")

// Quan ly luu tru token dang nhap + thong tin nhan vien hien tai (dung DataStore)
object SessionManager {
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val STAFF_NAME_KEY = stringPreferencesKey("staff_name")
    private val STAFF_ROLE_KEY = stringPreferencesKey("staff_role")

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun saveSession(token: String, staffName: String, role: String) {
        appContext.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[STAFF_NAME_KEY] = staffName
            prefs[STAFF_ROLE_KEY] = role
        }
    }

    suspend fun clearSession() {
        appContext.dataStore.edit { it.clear() }
    }

    fun tokenFlow(): Flow<String?> = appContext.dataStore.data.map { it[TOKEN_KEY] }

    fun getTokenBlocking(): String? = runBlocking { tokenFlow().first() }

    suspend fun getStaffName(): String? = appContext.dataStore.data.map { it[STAFF_NAME_KEY] }.first()
    suspend fun getStaffRole(): String? = appContext.dataStore.data.map { it[STAFF_ROLE_KEY] }.first()
}
