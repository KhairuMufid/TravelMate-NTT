package com.example.travelmatentt.fragment

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.travelmatentt.R

class Filter2Activity : AppCompatActivity() {

    private lateinit var destinationContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter2)

        destinationContainer = findViewById(R.id.destinationContainer)

        // Call function to add 10 cards
        addDestinationCards()
    }

    private fun addDestinationCards() {
        // Sample image and text for cards
        val cardData = listOf(
            Pair(R.drawable.pantai1, "Pantai Cincin"), // Replace with actual drawable names
            Pair(R.drawable.pantai2, "Pantai Napae"),
            Pair(R.drawable.pantai3, "Pantai Kita"),
            Pair(R.drawable.pantai4, "Pantai Paradiso"),
            Pair(R.drawable.pantai5, "Pantai Iteng"),
            Pair(R.drawable.destination_image1,   "Weekuri Lago"),
            Pair(R.drawable.destination_image2, "Pantai Marosi"),
            Pair(R.drawable.destination_image3, "Pantai Pero"),
            Pair(R.drawable.destination_image4, "Pantai Kelapa Gading"),
            Pair(R.drawable.destination_image5, "Pantai Onabalu")
        )

        cardData.forEach { (imageRes, title) ->
            addDestinationCard(imageRes, title)
        }
    }

    private fun addDestinationCard(imageRes: Int, title: String) {
        val cardView = layoutInflater.inflate(R.layout.item_card_destination, destinationContainer, false)

        val imageView = cardView.findViewById<ImageView>(R.id.cardImageView)
        val titleTextView = cardView.findViewById<TextView>(R.id.cardTitleTextView)
        val descriptionTextView = cardView.findViewById<TextView>(R.id.cardDescriptionTextView)

        // Set image from drawable
        imageView.setImageResource(imageRes)

        // Set title and description (description can be a static or dynamic text)
        titleTextView.text = title
        descriptionTextView.text = "Short description for $title"  // You can customize this text

        // Add the card to the container
        destinationContainer.addView(cardView)
    }
}
