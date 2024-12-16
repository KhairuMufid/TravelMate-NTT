package com.example.travelmatentt.view.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _userLoggedIn = MutableLiveData<Boolean>()
    val userLoggedIn: LiveData<Boolean> = _userLoggedIn

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    fun setUserLoggedIn(isLoggedIn: Boolean) {
        _userLoggedIn.value = isLoggedIn
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    // Bisa ditambahkan lebih banyak fungsi untuk status aplikasi lainnya
}