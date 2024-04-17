package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.chuxu.R
import com.example.chuxu.controller.UserController
import com.example.chuxu.UIUtil
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connexion)

        // Activer le mode "Edge-to-Edge" pour étendre le contenu sur les bords de l'écran
        enableEdgeToEdge()
        // Masquer la barre de navigation et la barre de statut
        UIUtil.toggleSystemUI(this)

        //Initialisation du bouton d'Inscription
        val createAccountButton = findViewById<Button>(R.id.Inscrip)

        // Initialisation des champs de texte et du bouton de connexion
        emailEditText = findViewById(R.id.Email)
        passwordEditText = findViewById(R.id.PasswordVerif)
        val connectButton = findViewById<Button>(R.id.Connect)
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)

        // Ajouter un écouteur de clic sur le bouton "Créer un compte"
        createAccountButton.setOnClickListener {
            // Rediriger l'utilisateur vers Activity3
            val intent = Intent(this, Activity3::class.java)
            startActivity(intent)
        }

        // Définition de l'action à effectuer lors du clic sur le bouton de connexion
        connectButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Vérification si les champs email et password ne sont pas vides
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez saisir votre email et votre mot de passe.", Toast.LENGTH_LONG).show()
            } else {
                // Exécution de la tâche de connexion dans une coroutine
                CoroutineScope(Dispatchers.Main).launch {
                    val (passwordMatch, nickname) = UserController.loginUser(email, password)
                    if (passwordMatch) {
                        // Redirection vers la page suivante si l'utilisateur est connecté avec succès
                        val intent = Intent(this@MainActivity, Activity2::class.java)
                        startActivity(intent)

                        // Enregistrement de l'état de connexion et du nickname de l'utilisateur
                        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putBoolean("isUserLoggedIn", true)
                        editor.putString("userEmail", email)
                        editor.putString("userNickname", nickname)
                        editor.apply()
                    } else {
                        // Affichage d'un message d'erreur si les identifiants sont incorrects
                        Toast.makeText(this@MainActivity, "Identifiants incorrects. Veuillez réessayer.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Vérification de l'état de connexion au démarrage de l'activité
        val isUserLoggedIn = sharedPref.getBoolean("isUserLoggedIn", false)
        if (isUserLoggedIn) {
            // Redirection vers la page suivante si l'utilisateur est déjà connecté
            val intent = Intent(this, Activity2::class.java)
            startActivity(intent)
            finish()
        }
    }
}