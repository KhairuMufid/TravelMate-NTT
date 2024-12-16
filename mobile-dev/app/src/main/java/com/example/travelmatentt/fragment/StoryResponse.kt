package com.example.travelmatentt.fragment



data class Story(
    val id: String,
    val userId: String,
    val content: String,
    val rating: Double,
    val likes: Int,
    val likedBy: List<String>? = null,
    val updatedAt: CreatedAt? = null,
    val media: List<String>,
    val createdAt: CreatedAt
)
data class CreatedAt(
    val _seconds: Long,
    val _nanoseconds: Int
)




