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
import kotlinx.coroutines.*

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

        // Activer le mode "Edge-to-Edge" pour étendre le contenu sur les bords de l'écran
        enableEdgeToEdge()

        // Initialisation des champs de texte et du bouton de connexion
        emailEditText = findViewById(R.id.Email)
        passwordEditText = findViewById(R.id.PasswordVerif)
        val connectButton = findViewById<Button>(R.id.Connect)
        val createAccountButton = findViewById<Button>(R.id.Inscrip)

        // Ajouter un écouteur de clic sur le bouton "Créer un compte"
        createAccountButton.setOnClickListener {
            // Rediriger l'utilisateur vers Activity3
            val intent = Intent(this, Activity3::class.java)
            startActivity(intent)
        }

        // Activer le bouton de connexion (Obligatoire si l'utilisateur s'est déconnecté car mis en false plus bas)
        connectButton.isEnabled = true

        connectButton.setOnClickListener {
            //Désactiver le bouton pour éviter les spams (Cela peut créer du lag)
            connectButton.isEnabled = false

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Si les champs email et password ne sont pas vides
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez saisir votre email et votre mot de passe.", Toast.LENGTH_LONG).show()
                connectButton.isEnabled = true
            } else {
                // Exécution de la tâche de connexion dans une coroutine (Pour éviter de l'exécuter dans l'application, ce qui est INTERDIT)
                CoroutineScope(Dispatchers.Main).launch {
                    showLoadingView("Connexion...", "Veuillez patienter...")
                    val (passwordMatch, nickname) = UserController.loginUser(email, password)
                    if (passwordMatch) {

                        // Récupération de l'ID utilisateur
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
                            connectButton.isEnabled = true
                        }
                    } else {
                        hideLoadingView()
                        Toast.makeText(this@MainActivity, "Identifiants incorrects. Veuillez réessayer.", Toast.LENGTH_LONG).show()
                        connectButton.isEnabled = true
                    }
                }
            }
        }

        // Vérification de l'état de connexion au démarrage de l'activité
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        val isUserLoggedIn = sharedPref.getBoolean("isUserLoggedIn", false)
        if (isUserLoggedIn) {
            // Redirection vers la page suivante si l'utilisateur est déjà connecté
            val intent = Intent(this, Activity2::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Fonction pour inflate la page actuelle et rajouter un chargement
    private fun showLoadingView(text1: String, text2: String) {
        // Rajouter une vérification (Pour éviter plusieurs loading screen en même temps)
        if (!isShowingLoadingView) {
            isShowingLoadingView = true
            loadingView = layoutInflater.inflate(R.layout.loading_screen, findViewById(android.R.id.content), false)
            loadingTextView1 = loadingView.findViewById(R.id.loadingtextView1)
            loadingTextView2 = loadingView.findViewById(R.id.loadingtextView2)
            loadingTextView1.text = text1
            loadingTextView2.text = text2
            (findViewById<ViewGroup>(android.R.id.content)).addView(loadingView)
        } else {
            // Mettre à jour le texte de l'écran de chargement existant
            updateLoadingView(text1, text2)
        }
    }

    // Fonction pour cacher la page de chargement
    private fun hideLoadingView() {
        isShowingLoadingView = false
        (findViewById<ViewGroup>(android.R.id.content)).removeView(loadingView)
    }

    private fun updateLoadingView(text1: String, text2: String) {
        loadingTextView1.text = text1
        loadingTextView2.text = text2
    }
}