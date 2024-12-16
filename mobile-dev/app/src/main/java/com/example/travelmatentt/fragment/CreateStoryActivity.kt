package com.example.travelmatentt.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travelmatentt.R
import com.example.travelmatentt.data.retrofit.RetrofitClient
import com.example.travelmatentt.fragment.ResponseStory
import com.example.travelmatentt.fragment.StoryRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CreateStoryActivity : AppCompatActivity() {

    private lateinit var etStoryContent: EditText
    private lateinit var ivStoryImage: ImageView
    private lateinit var ratingBarStory: RatingBar
    private lateinit var btnSaveStory: Button

    private var accessToken: String? = null
    private val PICK_DOCUMENT_REQUEST = 1
    private var storyImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_create_story)


        etStoryContent = findViewById(R.id.etStoryContent)
        ivStoryImage = findViewById(R.id.ivStoryImage)
        ratingBarStory = findViewById(R.id.ratingBarStory)
        btnSaveStory = findViewById(R.id.btnSaveStory)


        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        accessToken = sharedPreferences.getString("access_token", null)


        ivStoryImage.setOnClickListener {
            openFilePicker()
        }

        btnSaveStory.setOnClickListener {
            saveStory()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_DOCUMENT_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_DOCUMENT_REQUEST) {
            storyImageUri = data?.data
            ivStoryImage.setImageURI(storyImageUri)
        } else {
            Toast.makeText(this, "File selection failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStory() {
        val storyContent = etStoryContent.text.toString()
        val storyRating = ratingBarStory.rating

        if (storyContent.isNotEmpty() && storyImageUri != null) {

            val storageReference =
                FirebaseStorage.getInstance().reference.child("story-media/${UUID.randomUUID()}.jpg")
            val uploadTask = storageReference.putFile(storyImageUri!!)

            uploadTask.addOnSuccessListener { taskSnapshot ->

                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val mediaUrl = uri.toString()

                    val storyRequest = StoryRequest(
                        message = storyContent,
                        mediaURLs = listOf(mediaUrl)
                    )

                    accessToken?.let {
                        val call = RetrofitClient.instance.createStory("Bearer $it", storyRequest)

                        call.enqueue(object : Callback<ResponseStory> {
                            override fun onResponse(
                                call: Call<ResponseStory>,
                                response: Response<ResponseStory>
                            ) {
                                if (response.isSuccessful) {
                                    val responseBody = response.body()
                                    responseBody?.let {

                                        val storyId = it.storyId
                                        if (storyId != null) {

                                            Toast.makeText(
                                                this@CreateStoryActivity,
                                                "Story saved successfully with ID: $storyId",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {

                                            Toast.makeText(
                                                this@CreateStoryActivity,
                                                "Story saved successfully, but no storyId returned.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        this@CreateStoryActivity,
                                        "Error: ${response.message()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<ResponseStory>, t: Throwable) {
                                Toast.makeText(
                                    this@CreateStoryActivity,
                                    "Failed to save story: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }
}