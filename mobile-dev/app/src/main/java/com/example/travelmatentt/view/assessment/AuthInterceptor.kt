package com.example.travelmatentt.view.assessment

import okhttp3.Interceptor
import okhttp3.Response

// Interceptor untuk menambahkan token ke header
class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token") // Menambahkan token ke header
            .build()
        return chain.proceed(request)
    }
}
