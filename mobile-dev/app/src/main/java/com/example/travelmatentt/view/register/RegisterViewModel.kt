package com.example.travelmatentt.view.register

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.travelmatentt.data.request.RegisterRequest
import com.example.travelmatentt.data.retrofit.ApiConfig
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val _registrationStatus = MutableLiveData<String>()
    val registrationStatus: LiveData<String> = _registrationStatus

    fun validateInput(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registrationStatus.value = "Invalid email format!"
            return false
        }
        if (username.length < 3) {
            _registrationStatus.value = "Username must be at least 3 characters!"
            return false
        }
        if (password.length < 6) {
            _registrationStatus.value = "Password must be at least 6 characters!"
            return false
        }
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _registrationStatus.value = "All fields are required!"
            return false
        }
        if (password != confirmPassword) {
            _registrationStatus.value = "Passwords do not match!"
            return false
        }
        return true
    }

    fun registerUser(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val registerRequest = RegisterRequest(username, email, password, confirmPassword)
                val response = apiService.register(registerRequest)

                if (response.message == "User registered successfully") {
                    _registrationStatus.value = "Registration successful!"
                } else {
                    _registrationStatus.value = "Registration failed: ${response.message}"
                }
            } catch (e: Exception) {
                _registrationStatus.value = "An error occurred: ${e.message}"
            }
        }
    }
}