package com.example.travelmatentt.data.retrofit

import com.example.travelmatentt.data.request.LoginRequest
import com.example.travelmatentt.data.request.RegisterRequest
import com.example.travelmatentt.data.response.LoginResponse
import com.example.travelmatentt.data.response.RegisterResponse
import com.example.travelmatentt.data.response.UserResponse
import com.example.travelmatentt.fragment.DeleteResponse
import com.example.travelmatentt.fragment.DestinationResponse
import com.example.travelmatentt.fragment.RecommendationResponse
import com.example.travelmatentt.fragment.ResponseStory
import com.example.travelmatentt.fragment.Story
import com.example.travelmatentt.fragment.StoryRequest
import com.example.travelmatentt.fragment.WishlistItem
import com.example.travelmatentt.model.WishlistRequest
import com.example.travelmatentt.fragment.WishlistResponse
import com.example.travelmatentt.view.assessment.AssessmentRequest
import com.example.travelmatentt.view.assessment.AssessmentResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("user")
    fun getUser(
        @Header("Authorization") token: String
    ): Call<UserResponse>

    @POST("register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): RegisterResponse

    @POST("login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @POST("assessment")
    fun submitAssessment(@Body assessmentRequest: AssessmentRequest): Call<AssessmentResponse>

    @GET("recommend-destinations")
    fun getDestinationRecommendations(@Header("Authorization") token: String)
    : Call<RecommendationResponse>

    @GET("stories")
    fun getStory(@Header("Authorization") token: String): Call<List<Story>>

    @POST("/stories")
    fun createStory(
        @Header("Authorization") accessToken: String,
        @Body storyRequest: StoryRequest
    ): Call<ResponseStory>

    @GET("wishlist")
    fun getWishlist(@Header("Authorization") token: String): Call<List<WishlistItem>>

    @POST("wishlist")
    fun addToWishlist(
        @Header("Authorization") token: String,
        @Body wishlistRequest: WishlistRequest
    ): Call<WishlistResponse>

    @DELETE("wishlist/{id}")
    fun deleteWishlistItem(
        @Header("Authorization") token: String,
        @Path("id") wishlistId: String
    ): Call<DeleteResponse>

    @GET("destinations/{jenis}")
    fun getDestinations(
        @Header("Authorization") token: String,
        @Path("jenis") jenis: String
    ): Call<List<DestinationResponse>>

    @GET("similar-destinations/{namaObjek}")
    fun getSimilarDestinations(
        @Header("Authorization") token: String,
        @Path("namaObjek") namaObjek: String
    ): Call<DestinationResponse>}

