package com.example.chuxu.view

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.R
import com.example.chuxu.controller.ReviewsController
import com.example.chuxu.controller.ReviewsController.fetchUserGameReviews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activité tierciaire de l'application, permet à l'utilisateur de consulter tous ses avis sur différents jeux
 */
class Activity8 : AppCompatActivity(), GameReviewModelAdapter.OnDeleteReviewClickListener, GameReviewModelAdapter.OnModifyReviewClickListener {

    private var isShowingLoadingView = false
    private lateinit var loadingView: View
    private lateinit var loadingTextView1: TextView
    private lateinit var loadingTextView2: TextView
    private lateinit var adapter: GameReviewModelAdapter
    private lateinit var recyclerView: RecyclerView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reviews)

        adapter = GameReviewModelAdapter()
        adapter.setDeleteReviewClickListener(this)
        adapter.setModifyReviewClickListener(this)

        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        val userID = sharedPref.getInt("userID", 0)
        println(userID)
        showLoadingView("Nous récupérons vos avis...","Un instant...")

        CoroutineScope(Dispatchers.Main).launch {
            val reviews = fetchUserGameReviews(userID)
            if (reviews.isEmpty()) {
                hideLoadingView()
                Toast.makeText(this@Activity8, "Vous n'avez déposé aucun avis.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                hideLoadingView()
                recyclerView = findViewById(R.id.myReviewRecyclerView)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this@Activity8)
                adapter.setData(reviews)
            }
        }
    }

    override fun onDeleteReviewClicked(userID: Int, reviewID: Int){
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        alertDialogBuilder.apply {
            setTitle("Confirmation")
            setMessage("Êtes-vous sûr de vouloir supprimer votre avis ?")
            setPositiveButton("Oui") { _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
                    val success = ReviewsController.deleteUserReview(userID, reviewID)
                    if (success) {
                        Toast.makeText(this@Activity8, "Votre avis a bien été effacé !", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@Activity8, "Une erreur est survenue.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            setNegativeButton("Non") { _, _ -> }
        }
        val alertDialog = alertDialogBuilder.create()

        alertDialog.show()

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.red))
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    override fun onModifyReviewsClicked(userID: Int, reviewID: Int){
        CoroutineScope(Dispatchers.Main).launch {
            val gameInfo = ReviewsController.modifyUserReview(userID, reviewID)
            if (gameInfo != null) {
                val intent = Intent(this@Activity8, Activity6::class.java)
                intent.putExtra("appId", gameInfo.first)
                intent.putExtra("appName", gameInfo.second)
                startActivity(intent)
            } else {
                Toast.makeText(this@Activity8, "Une erreur est survenue.", Toast.LENGTH_LONG).show()
            }
        }
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