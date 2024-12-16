package com.example.travelmatentt.view.welcome

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class WelcomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserLoggedIn: LiveData<Boolean> get() = _isUserLoggedIn

    init {
        checkUserLoginStatus()
    }

    private fun checkUserLoginStatus() {
        val accessToken = sharedPreferences.getString("access_token", null)
        _isUserLoggedIn.value = !accessToken.isNullOrEmpty()
    }

}