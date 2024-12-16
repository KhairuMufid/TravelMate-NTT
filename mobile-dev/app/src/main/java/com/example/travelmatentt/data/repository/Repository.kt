package com.example.travelmatentt.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.travelmatentt.data.database.DestinationDatabase
import com.example.travelmatentt.data.preference.UserModel
import com.example.travelmatentt.data.preference.UserPreference
import com.example.travelmatentt.data.request.LoginRequest
import com.example.travelmatentt.data.request.RegisterRequest
import com.example.travelmatentt.data.response.ErrorResponse
import com.example.travelmatentt.data.response.LoginResponse
import com.example.travelmatentt.data.response.RegisterResponse
import com.example.travelmatentt.data.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import com.example.travelmatentt.data.result.Result

class Repository{
}