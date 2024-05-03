package com.example.chuxu.view

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.R
import com.example.chuxu.controller.UserController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Activity7 : AppCompatActivity() {

    private var isShowingLoadingView = false
    private lateinit var loadingView: View
    private lateinit var loadingTextView1: TextView
    private lateinit var loadingTextView2: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reviews)

        val appId = intent.getIntExtra("appId", 0)
        showLoadingView("Nous récupérons les avis...","Un instant...")
        fetchGameReviews(appId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchGameReviews(appId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val reviews = UserController.fetchGameReviews(appId)
            if (reviews.isEmpty()) {
                hideLoadingView()
                Toast.makeText(this@Activity7, "Aucun avis disponible pour ce jeu.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                hideLoadingView()
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

    private fun showLoadingView(text1: String, text2: String) {
        if (!isShowingLoadingView) {
            isShowingLoadingView = true
            loadingView = layoutInflater.inflate(R.layout.loading_screen, findViewById(android.R.id.content), false)
            loadingTextView1 = loadingView.findViewById(R.id.loadingtextView1)
            loadingTextView2 = loadingView.findViewById(R.id.loadingtextView2)
            loadingTextView1.text = text1
            loadingTextView2.text = text2
            (findViewById<ViewGroup>(android.R.id.content)).addView(loadingView)
        } else {
            updateLoadingView(text1, text2)
        }
    }

    private fun hideLoadingView() {
        isShowingLoadingView = false
        (findViewById<ViewGroup>(android.R.id.content)).removeView(loadingView)
    }

    private fun updateLoadingView(text1: String, text2: String) {
        loadingTextView1.text = text1
        loadingTextView2.text = text2
    }
}

