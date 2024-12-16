package com.example.travelmatentt.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.travelmatentt.Maps.MapsActivity
import com.example.travelmatentt.R
import com.example.travelmatentt.data.retrofit.ApiService
import com.example.travelmatentt.databinding.ActivityDestinationDetailBinding
import com.example.travelmatentt.model.WishlistRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DestinationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDestinationDetailBinding

    private var isLoved = false
    private var accessToken: String? = null
    private lateinit var cardAdapter: CardAdapter

    private val hotelNames = listOf("Hotel Swiss-Bellin Kristal Kupang", "Kanawa Bungalow Beach Resort", "Aston Hotel Kupang", "Harper Kupang", "Sotis Hotel Kupang")
    private val hotelImages = listOf(
        R.drawable.hotel1, R.drawable.hotel2, R.drawable.hotel3, R.drawable.hotel4, R.drawable.hotel5
    )
    private val hotelRatings = listOf(4.0f, 4.5f, 3.5f, 5.0f, 4.0f)

    private val destinationCards = listOf(
        listOf("Weekuri Lago", "Pantai Marosi", "Pantai Pero", "Pantai Kelapa Gading", "Pantai Onabalu"),
        listOf("Gunung Inerie", "Pantai Oeseli", "Danau Kelimutu", "Nihi Watu Resort Sumba", "Oenesu")
    )

    private val destinationImages = listOf(
        listOf(R.drawable.destination_image1, R.drawable.destination_image2, R.drawable.destination_image3, R.drawable.destination_image4, R.drawable.destination_image5),
        listOf(R.drawable.gununginerie, R.drawable.pantaioeseli, R.drawable.danaukelimutu, R.drawable.nihiwaturesortsumba, R.drawable.oenesu)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDestinationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val destinationRatings = listOf(
            4.5f, 3.0f, 4.0f, 5.0f, 2.5f
        )

        // Set up RecyclerView for hotel recommendations
        val hotelAdapter = CardAdapter(this, hotelNames, hotelImages, hotelRatings)
        binding.rvHotelCards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvHotelCards.adapter = hotelAdapter

        // Set up RecyclerView for destination recommendations
        cardAdapter = CardAdapter(this, destinationCards[0], destinationImages[0], destinationRatings)
        binding.rvHorizontalCards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvHorizontalCards.adapter = cardAdapter

        val recyclerView = binding.rvHorizontalCards
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = CardAdapter(this, destinationCards[0], destinationImages[0], destinationRatings)
        recyclerView.adapter = adapter

        binding.btnSiteMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        binding.btnStory.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            val storyFragment = StoryFragment()

            val bundle = Bundle()
            bundle.putString("access_token", accessToken)
            storyFragment.arguments = bundle
            transaction.replace(R.id.fragment_container, storyFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val namaObjek = intent.getStringExtra("nama_objek")
        val deskripsi = intent.getStringExtra("deskripsi")
        val pictureUrl = intent.getStringExtra("picture_url")
        val alamat = intent.getStringExtra("alamat")

        val ratingString = intent.getStringExtra("rating")
        val rating = ratingString?.toFloatOrNull() ?: 0.0f

        val estimasiHargaTiket = intent.getStringExtra("estimasi_harga_tiket")

        binding.tvDestinationName.text = namaObjek
        binding.tvDescription.text = deskripsi
        binding.tvAlamat.text = "Alamat: $alamat"
        binding.tvEstimasiHargaTiket.text = "Estimasi Harga Tiket: $estimasiHargaTiket"

        if (!pictureUrl.isNullOrEmpty()) {
            Log.d("DestinationDetailActivity", "Loading image from: $pictureUrl")  // Log URL
            Glide.with(this)
                .load(pictureUrl)
                .into(binding.ivPicture)
        } else {
            Log.d("DestinationDetailActivity", "Image URL is empty or invalid, using default image.")
            Glide.with(this)
                .load(R.drawable.uploadimage)
                .into(binding.ivPicture)
        }

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        accessToken = sharedPreferences.getString("access_token", null)

        val ivLove: ImageView = findViewById(R.id.ivLove)
        ivLove.setOnClickListener {
            if (isLoved) {
                ivLove.setImageResource(R.drawable.ic_love)
                isLoved = false
            } else {
                ivLove.setImageResource(R.drawable.ic_love_filled)
                isLoved = true

                accessToken?.let { token ->
                    addToWishlist(token, namaObjek)
                } ?: run {
                    Toast.makeText(this@DestinationDetailActivity, "Access token is missing", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addToWishlist(token: String, namaObjek: String?) {
        if (namaObjek.isNullOrEmpty()) {
            Toast.makeText(this, "Destination name is invalid", Toast.LENGTH_SHORT).show()
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://travelmate-ntt-1096623490059.asia-southeast2.run.app/") // Gantilah dengan URL API yang sesuai
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val wishlistRequest = WishlistRequest(message = "Wishlist added successfully", nama_objek = namaObjek)
        val call = apiService.addToWishlist("Bearer $token", wishlistRequest)

        call.enqueue(object : Callback<WishlistResponse> {
            override fun onResponse(call: Call<WishlistResponse>, response: Response<WishlistResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DestinationDetailActivity, "Added to wishlist", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("DestinationDetailActivity", "Error: $errorMessage")
                    Toast.makeText(this@DestinationDetailActivity, "Failed to add to wishlist: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WishlistResponse>, t: Throwable) {
                Log.e("DestinationDetailActivity", "Failure: ${t.message}")
                Toast.makeText(this@DestinationDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
