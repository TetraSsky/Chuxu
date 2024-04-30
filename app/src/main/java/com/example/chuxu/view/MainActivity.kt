package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.chuxu.R
import com.example.chuxu.controller.UserController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activité principale de l'application, gère la connexion de l'utilisateur
 */
class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private var isShowingLoadingView = false
    private lateinit var loadingView: View
    private lateinit var loadingTextView1: TextView
    private lateinit var loadingTextView2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connexion)

        enableEdgeToEdge()

        emailEditText = findViewById(R.id.Email)
        passwordEditText = findViewById(R.id.PasswordVerif)
        val connectButton = findViewById<Button>(R.id.Connect)
        val createAccountButton = findViewById<Button>(R.id.Inscrip)

        createAccountButton.setOnClickListener {
            val intent = Intent(this, Activity3::class.java)
            startActivity(intent)
        }

        connectButton.isEnabled = true
        connectButton.setOnClickListener {
            connectButton.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Veuillez saisir votre email et votre mot de passe.", Toast.LENGTH_LONG).show()
                    delay(4000)
                    connectButton.isEnabled = true
                } else {
                    showLoadingView("Connexion...", "Veuillez patienter...")
                    val (passwordMatch, nickname) = UserController.loginUser(email, password)
                    if (passwordMatch) {

                        val userID = UserController.getUserID(email)

                        if (userID != 0) {
                            val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            editor.putBoolean("isUserLoggedIn", true)
                            editor.putString("userEmail", email)
                            editor.putString("userNickname", nickname)
                            editor.putInt("userID", userID)
                            editor.apply()

                            hideLoadingView()

                            val intent = Intent(this@MainActivity, Activity2::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            hideLoadingView()
                            Toast.makeText(this@MainActivity, "Une erreur est survenue.", Toast.LENGTH_LONG).show()
                            delay(4000)
                            connectButton.isEnabled = true
                        }
                    } else {
                        hideLoadingView()
                        Toast.makeText(this@MainActivity, "Identifiants incorrects. Veuillez réessayer.", Toast.LENGTH_LONG).show()
                        delay(4000)
                        connectButton.isEnabled = true
                    }
                }
            }
        }
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        val isUserLoggedIn = sharedPref.getBoolean("isUserLoggedIn", false)
        if (isUserLoggedIn) {
            val intent = Intent(this, Activity2::class.java)
            startActivity(intent)
            finish()
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