package com.example.travelmatentt.fragment

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.travelmatentt.R
import com.example.travelmatentt.data.retrofit.ApiConfig
import com.example.travelmatentt.databinding.ActivityDetailWishlistBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class DeleteResponse(
    val message: String
)

class DetailWishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailWishlistBinding
    private var isFavorited = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val namaObjek = intent.getStringExtra("nama_objek")
        val deskripsi = intent.getStringExtra("deskripsi")
        val pictureUrl = intent.getStringExtra("picture_url")
        val rating = intent.getStringExtra("rating")
        val alamat = intent.getStringExtra("alamat")
        val estimasiHargaTik = intent.getStringExtra("estimasi_harga_tiket")
        val wishlistId = intent.getStringExtra("wishlist_id")

        binding.tvNamaObjek.text = namaObjek ?: "Nama Objek Tidak Ditemukan"
        binding.tvDeskripsi.text = deskripsi ?: "Deskripsi Tidak Tersedia"
        binding.tvRating.text = rating ?: "Rating Tidak Tersedia"
        binding.tvAlamat.text = alamat ?: "Alamat Tidak Tersedia"
        binding.tvEstimasiHarga.text = estimasiHargaTik ?: "Estimasi Harga Tidak Tersedia"

        if (!pictureUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(pictureUrl)
                .into(binding.ivDestinationImage)
        } else {
            binding.ivDestinationImage.setImageResource(R.drawable.uploadimage)
        }

        binding.ivLove.setImageResource(R.drawable.ic_love_filled)

        binding.ivLove.setOnClickListener {
            if (isFavorited) {
                binding.ivLove.setImageResource(R.drawable.ic_love)
                if (!wishlistId.isNullOrEmpty()) {
                    deleteWishlistItem(wishlistId)
                }
            } else {
                binding.ivLove.setImageResource(R.drawable.ic_love_filled)
            }
            isFavorited = !isFavorited
        }
    }

    private fun deleteWishlistItem(wishlistId: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)

        if (accessToken.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val call = ApiConfig.getApiService().deleteWishlistItem("Bearer $accessToken", wishlistId)

        call.enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Wishlist berhasil dihapus"
                    Toast.makeText(this@DetailWishlistActivity, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this@DetailWishlistActivity,
                        "Gagal menghapus wishlist: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                Toast.makeText(
                    this@DetailWishlistActivity,
                    "Gagal menghapus wishlist: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
