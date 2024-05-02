package com.example.chuxu.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.R
import com.example.chuxu.controller.UserController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Activity7 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reviews)

        val appId = intent.getIntExtra("appId", 0)

        fetchGameReviews(appId)
    }

    private fun fetchGameReviews(appId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val reviews = UserController.fetchGameReviews(appId)
            if (reviews.isEmpty()) {
                Toast.makeText(this@Activity7, "Aucun avis disponible pour ce jeu.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                displayReviews(reviews)
            }
        }
    }

    private fun displayReviews(reviews: List<GameReviewModel>) {
        val recyclerView: RecyclerView = findViewById(R.id.myReviewRecyclerView)
        val adapter = GameReviewModelAdapter()
        adapter.setData(reviews)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

