package com.example.travelmatentt.view.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.travelmatentt.data.request.LoginRequest
import com.example.travelmatentt.data.retrofit.ApiConfig
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> get() = _loginStatus

    private val sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = ApiConfig.getApiService().login(loginRequest)

                if (!response.accessToken.isNullOrEmpty()) {
                    saveTokens(response.accessToken, response.refreshToken)
                    _loginStatus.postValue(true)
                } else {
                    _loginStatus.postValue(false)
                }
            } catch (e: Exception) {
                _loginStatus.postValue(false)
            }
        }
    }

    private fun saveTokens(accessToken: String, refreshToken: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("access_token", accessToken)
        editor.putString("refresh_token", refreshToken)
        editor.apply()
    }
}