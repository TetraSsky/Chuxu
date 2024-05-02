package com.example.chuxu.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.chuxu.R
import com.example.chuxu.controller.UserController
import kotlinx.coroutines.*

/**
 * Activité tierciaire de l'application, permet à l'utilisateur de laisser une review à un jeu
 */
class Activity6 : AppCompatActivity() {

    private lateinit var sendReviewButton: Button
    private lateinit var appIdTextView: TextView
    private lateinit var appNameTextView: TextView
    private lateinit var userNicknameTextView: TextView
    private lateinit var userReviewEditTextView: EditText
    private var isShowingLoadingView = false
    private lateinit var loadingView: View
    private lateinit var loadingTextView1: TextView
    private lateinit var loadingTextView2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review)

        enableEdgeToEdge()

        appIdTextView = findViewById(R.id.gameID)
        appNameTextView = findViewById(R.id.gameName)
        userNicknameTextView = findViewById(R.id.TextView1)
        userReviewEditTextView = findViewById(R.id.userReview)
        sendReviewButton = findViewById(R.id.sendReview)

        val appId = intent.getIntExtra("appId", 0)
        val appName = intent.getStringExtra("appName")
        val userNickname = intent.getStringExtra("userNickname")
        val userID = intent.getIntExtra("userID", 0)

        appIdTextView.text = "$appId"
        appNameTextView.text = "$appName"
        userNicknameTextView.text = "Laissez un avis sur ce jeu, $userNickname !"

        sendReviewButton.isEnabled = true
        sendReviewButton.setOnClickListener {
            showLoadingView("Publication en cours...", "Veuillez patienter...")
            sendReviewButton.isEnabled = false
            val userReview = userReviewEditTextView.text.toString()
            CoroutineScope(Dispatchers.Main).launch {
                if (userReview.isEmpty()) {
                    hideLoadingView()
                    Toast.makeText(this@Activity6, "Veuillez saisir votre avis.", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    sendReviewButton.isEnabled = true
                } else if (userReview.length < 50) {
                    hideLoadingView()
                    Toast.makeText(this@Activity6, "Votre avis doit contenir au moins 50 caractères.", Toast.LENGTH_SHORT).show()
                    delay(4000)
                    sendReviewButton.isEnabled = true
                } else {
                    val result = UserController.createReview(userID, appId, userReview, appName)
                    if (result) {
                        hideLoadingView()
                        Toast.makeText(this@Activity6, "Votre avis a été ajouté avec succès.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        hideLoadingView()
                        Toast.makeText(this@Activity6, "Une erreur s'est produite lors de l'ajout de votre avis.", Toast.LENGTH_SHORT).show()
                        delay(4000)
                        sendReviewButton.isEnabled = true
                    }
                }
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