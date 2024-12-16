package com.example.travelmatentt.fragment

import androidx.navigation.ActivityNavigator

data class WishlistResponse(
    val message: String,
    val nama_objek: String,
    val data: List<String>,
    val id: String?,
    val picture_url: String?,
    val userId: String?,
    val destinationId: String?,
    val addedAt: CreatedAt?,
    val destination: ActivityNavigator.Destination?
)
