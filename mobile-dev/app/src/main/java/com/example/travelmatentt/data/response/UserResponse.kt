package com.example.travelmatentt.data.response

import com.google.gson.annotations.SerializedName

data class UserResponse(

	@field:SerializedName("user")
	val user: User? = null
)

data class User(

	@field:SerializedName("photoURL")
	val photoURL: String? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
