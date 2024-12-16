package com.example.travelmatentt.view.infoaccount

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travelmatentt.data.response.UserResponse
import com.example.travelmatentt.data.retrofit.ApiService
import com.example.travelmatentt.databinding.ActivityInfoAccountBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InfoAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoAccountBinding
    private val apiService: ApiService by lazy { createApiService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfoAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("access_token", null)

        if (!token.isNullOrEmpty()) {
            showLoading(true)
            fetchUserInfo(token)
        } else {
            showError("Token tidak ditemukan.")
        }
    }

    private fun fetchUserInfo(token: String) {
        apiService.getUser("Bearer $token").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    if (user != null) {
                        user.username?.let { user.email?.let { it1 -> displayUserInfo(it, it1) } }
                    } else {
                        showError("Data pengguna tidak tersedia.")
                    }
                } else {
                    showError("Gagal memuat informasi pengguna: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                showLoading(false)
                showError("Error: ${t.localizedMessage}")
            }
        })
    }

    private fun displayUserInfo(username: String, email: String) {
        binding.tvUsername.text = username
        binding.tvEmail.text = email
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun createApiService(): ApiService {
        val okHttpClient = OkHttpClient.Builder().build()
        return Retrofit.Builder()
            .baseUrl("https://travelmate-ntt-1096623490059.asia-southeast2.run.app/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

