package com.example.travelmatentt.data.preference

data class UserModel(
    val email: String,
    val username : String,
    val id: String,
    val isLogin: Boolean,
    val token: String? = null
)