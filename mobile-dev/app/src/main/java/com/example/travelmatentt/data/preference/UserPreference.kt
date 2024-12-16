package com.example.travelmatentt.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreference(context: Context) {

    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUser(user: UserModel) {
        val editor = prefs.edit()
        editor.putString("email", user.email)
        editor.putString("id", user.id)
        editor.putString("username", user.username)
        editor.putBoolean("isLogin", user.isLogin)
        editor.putString("token", user.token)
        editor.apply()
    }

    fun getUser(): UserModel {
        val email = prefs.getString("email", null)
        val id = prefs.getString("id", null)
        val username = prefs.getString("username", "")
        val isLogin = prefs.getBoolean("isLogin", false)
        val token = prefs.getString("token", null)

        return UserModel(
            username = username ?: "",
            email = email ?: "",
            id = id ?: "",
            isLogin = isLogin,
            token = token
        )
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean("isLogin", false)
    }
}
