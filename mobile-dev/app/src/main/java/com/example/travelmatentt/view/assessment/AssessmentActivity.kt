package com.example.travelmatentt.view.assessment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.travelmatentt.R
import com.example.travelmatentt.data.retrofit.ApiService
import com.example.travelmatentt.view.main.MainActivity
import com.example.travelmatentt.view.welcome.WelcomeActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AssessmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assesment)


        val touristAttractions = listOf(
            "Air Terjun", "Batu Karang", "Bukit", "Danau", "Desa Wisata",
            "Goa", "Gunung", "Pantai", "Pulau", "Sungai", "Taman",
            "Taman Nasional", "Tugu", "Wisata Alam"
        )
        val cities = listOf(
            "Alor", "Belu", "Ende", "Flores Timur", "Kota Kupang",
            "Kupang", "Lembata", "Malaka", "Manggarai", "Manggarai Barat",
            "Manggarai Timur", "Nagekeo", "Ngada", "Rote Ndao", "Sabu Raijua",
            "Sikka", "Sumba Barat", "Sumba Barat Daya", "Sumba Tengah",
            "Sumba Timur", "Timor Tengah Selatan", "Timor Tengah Utara"
        )


        val spinnerTourismType = findViewById<Spinner>(R.id.spinner_tourism_type)
        val spinnerkabupaten = findViewById<Spinner>(R.id.spinner_city)
        spinnerTourismType.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, touristAttractions
        )
        spinnerkabupaten.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, cities
        )


        val seekBar = findViewById<SeekBar>(R.id.seekbar_price)
        val priceLabel = findViewById<TextView>(R.id.tv_price_label)
        val priceNames = listOf(
            "Gratis (Rp 0)",
            "Terjangkau (Rp 0-20K)",
            "Menengah (Rp 20K-50K)",
            "Premium (Rp >50K)"
        )
        val priceRanges = listOf(
            0.0,
            20000.0,
            50000.0,
            100000.0
        )
        seekBar.max = priceRanges.size - 1

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val rangeLabel = when (progress.toFloat() / seekBar!!.max) {
                    in 0.0..0.2 -> "Free (Rp 0)"
                    in 0.2..0.4 -> "Affordable (Rp 1 - 20,000)"
                    in 0.4..0.6 -> "Mid Range (Rp 20,001 - 50,000)"
                    in 0.6..1.0 -> "Premium (Above Rp 50,000)"
                    else -> "Unknown"
                }
                priceLabel.text = rangeLabel
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null) ?: ""
        println("Access Token: $accessToken")


        val submitButton = findViewById<Button>(R.id.btn_submit)
        submitButton.setOnClickListener {
            if (accessToken.isNotEmpty()) {
                val selectedTourismType = spinnerTourismType.selectedItem.toString()
                val selectedkabupaten = spinnerkabupaten.selectedItem.toString()
                val selectedPriceRange = priceRanges[seekBar.progress] // Use the numeric value from priceRanges

                val retrofit = createRetrofitInstance(accessToken)
                val apiService = retrofit.create(ApiService::class.java)

                val assessmentRequest = AssessmentRequest(
                    preferences = listOf(
                        selectedkabupaten,
                        selectedTourismType,
                        selectedPriceRange
                    )
                )

                val call = apiService.submitAssessment(assessmentRequest)
                call.enqueue(object : Callback<AssessmentResponse> {
                    override fun onResponse(
                        call: Call<AssessmentResponse>,
                        response: Response<AssessmentResponse>
                    ) {
                        if (response.isSuccessful) {

                            val intent = Intent(this@AssessmentActivity, MainActivity::class.java)
                            startActivity(intent)
                        } else {

                            println("Response failed: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<AssessmentResponse>, t: Throwable) {

                        println("Error: ${t.message}")
                    }
                })
            }
        }
    }


    private fun createRetrofitInstance(token: String): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .build()

        return Retrofit.Builder()
            .baseUrl("https://travelmate-ntt-1096623490059.asia-southeast2.run.app/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
