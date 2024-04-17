package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chuxu.DatabaseManager
import com.example.chuxu.R
import com.example.chuxu.UIUtil
import com.example.chuxu.controller.UserController
import java.sql.SQLException

class Activity4 : AppCompatActivity(){

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nicknameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compteinfo)

        // Activer le mode "Edge-to-Edge" pour étendre le contenu sur les bords de l'écran
        enableEdgeToEdge()
        // Masquer la barre de navigation et la barre de statut
        UIUtil.toggleSystemUI(this)

        // Ajouter une écouteur pour ajuster le padding en fonction des barres de système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.compteinfo)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialisation des champs de texte et boutons
        emailEditText = findViewById(R.id.Email)
        passwordEditText = findViewById(R.id.PasswordVerif)
        nicknameEditText = findViewById(R.id.Nickname)
        val NouvEmailButton = findViewById<Button>(R.id.NouvEmail)
        val NouvMDPButton = findViewById<Button>(R.id.NouvMDP)
        val NouvNicknameButton = findViewById<Button>(R.id.NouvNickname)

        // Définition de l'action à effectuer lors du clic sur le bouton "NouvEmail"
        NouvEmailButton.setOnClickListener {
            val nouvEmail = emailEditText.text.toString().trim()
            val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
            val userID = sharedPref.getInt("userID", 0)
            val success = UserController.newUserEmail(userID, nouvEmail)
            if (success) {
                DatabaseManager.closeConnection()

                val editor = sharedPref.edit()
                editor.putBoolean("isUserLoggedIn", false)
                editor.apply()

                Toast.makeText(
                    this@Activity4, "Email modifié avec succès !", Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this@Activity4,
                    "Échec de la modification de l'email",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
        /*
                // Définition de l'action à effectuer lors du clic sur le bouton "NouvMDP"
                NouvMDPButton.setOnClickListener {
                    val nouvPassword = passwordEditText.text.toString().trim()
                    val success = UserController.newUserPassword(nouvPassword)
                    if (success) {

                    } else {

                    }
                }

                // Définition de l'action à effectuer lors du clic sur le bouton "NouvNickname"
                NouvNicknameButton.setOnClickListener {
                    val nouvNickname = nicknameEditText.text.toString().trim()
                    val success = UserController.newUserNickname(nouvNickname)
                    if (success) {

                    } else {

                    }
                }
         */
    }