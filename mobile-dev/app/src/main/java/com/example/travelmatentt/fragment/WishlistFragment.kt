package com.example.travelmatentt.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelmatentt.R
import com.example.travelmatentt.data.retrofit.ApiService
import com.example.travelmatentt.databinding.FragmentWishlistBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


data class Destination(
    val destinasi_wisata_id: String,
    val nama_objek: String?,
    val jenis_objek: String?,
    val koordinat: String?,
    val alamat: String?,
    val kabupaten_kota: String?,
    val rating: String?,
    val jumlah_reviewer: String?,
    val deskripsi: String?,
    val estimasi_harga_tiket: String?,
    val picture_url: String?
)

data class WishlistItem(
    val id: String?,
    val picture_url: String?,
    val userId: String?,
    val destinationId: String?,
    val addedAt: CreatedAt?,
    val destination: Destination?
)

class WishlistFragment : Fragment(R.layout.fragment_wishlist) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WishlistAdapter
    private var accessToken: String? = null
    private var binding: FragmentWishlistBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWishlistBinding.bind(view)


        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        accessToken = sharedPreferences.getString("access_token", null)


        Log.d("WishlistFragment", "Access Token: $accessToken")


        recyclerView = binding?.recyclerView ?: return
        adapter = WishlistAdapter { destination ->

            val context = requireContext()
            val intent = Intent(context, DetailWishlistActivity::class.java).apply {
                putExtra("nama_objek", destination.nama_objek)
                putExtra("deskripsi", destination.deskripsi)
                putExtra("picture_url", destination.picture_url)
                putExtra("rating", destination.rating)
                putExtra("alamat", destination.alamat)
                putExtra("estimasi_harga_tiket", destination.estimasi_harga_tiket)
            }
            context.startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter


        accessToken?.let {
            fetchWishlist(it)
        } ?: run {
            Log.e("WishlistFragment", "Access token is null!")
        }
    }

    private fun fetchWishlist(token: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://travelmate-ntt-1096623490059.asia-southeast2.run.app/") // Ganti URL backend yang sesuai
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getWishlist("Bearer $token")

        call.enqueue(object : Callback<List<WishlistItem>> {
            override fun onResponse(call: Call<List<WishlistItem>>, response: Response<List<WishlistItem>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("WishlistFragment", "Response Body: $responseBody") // Log isi response
                    val wishlistItems = responseBody?.distinctBy { it.id } ?: emptyList()  // Menghilangkan duplikasi berdasarkan id
                    adapter.submitList(wishlistItems)
                    Log.d("WishlistFragment", "Fetched wishlist successfully")
                } else {
                    Log.e("WishlistFragment", "Failed to fetch wishlist: ${response.code()} - ${response.message()}")
                    response.errorBody()?.let {
                        Log.e("WishlistFragment", "Error body: ${it.string()}")
                    }
                }
            }


            override fun onFailure(call: Call<List<WishlistItem>>, t: Throwable) {
                Log.e("WishlistFragment", "Error: ${t.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
    }
}