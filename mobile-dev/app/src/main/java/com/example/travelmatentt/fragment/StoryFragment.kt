package com.example.travelmatentt.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.travelmatentt.R
import com.example.travelmatentt.data.retrofit.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoryFragment : Fragment() {

    private var storyContent: TextView? = null
    private var storyImage: ImageView? = null
    private var storyRating: RatingBar? = null
    private var storyDate: TextView? = null
    private var accessToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_story, container, false)

        // Initialize the views
        storyContent = view.findViewById(R.id.storyContent)
        storyImage = view.findViewById(R.id.storyImage)
        storyRating = view.findViewById(R.id.storyRating)
        storyDate = view.findViewById(R.id.storyDate)

        // Get the access token from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        accessToken = sharedPreferences.getString("access_token", null)

        // If the access token is available, fetch the story
        accessToken?.let {
            fetchStory(it)
        }

        return view
    }

    private fun fetchStory(token: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://travelmate-ntt-1096623490059.asia-southeast2.run.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getStory("Bearer $token")

        call.enqueue(object : Callback<List<Story>> {
            override fun onResponse(call: Call<List<Story>>, response: Response<List<Story>>) {
                if (response.isSuccessful) {
                    val storyList = response.body()
                    storyList?.let { stories ->
                        if (stories.isNotEmpty()) {
                            val story = stories[0]
                            storyContent?.text = story.content
                            storyRating?.rating = story.rating.toFloat()

                            // If there is media (image), load it
                            if (story.media.isNotEmpty()) {
                                Picasso.get()
                                    .load(story.media[0])
                                    .into(storyImage)
                            }

                            // Format and set the date
                            val formattedDate = formatDate(story.createdAt)
                            storyDate?.text = "Created At: $formattedDate"
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Story>>, t: Throwable) {
                t.printStackTrace() // Log the error
            }
        })
    }

    private fun formatDate(timestamp: CreatedAt): String {
        val seconds = timestamp._seconds
        val date = Date(seconds * 1000)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }
}
