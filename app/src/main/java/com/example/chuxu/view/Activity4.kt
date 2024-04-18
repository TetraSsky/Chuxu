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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.SQLException
import java.util.regex.Pattern

class Activity4 : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVerifEditText : EditText
    private lateinit var nicknameEditText: EditText
    private val inappropriateNicknames = listOf("negro", "négro", "nègre")

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
        passwordEditText = findViewById(R.id.Password)
        passwordVerifEditText = findViewById(R.id.PasswordVerif)
        nicknameEditText = findViewById(R.id.Nickname)
        val NouvEmailButton = findViewById<Button>(R.id.NouvEmail)
        val NouvMDPButton = findViewById<Button>(R.id.NouvMDP)
        val NouvNicknameButton = findViewById<Button>(R.id.NouvNickname)

        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val passwordVerif = passwordVerifEditText.text.toString()
        val nickname = nicknameEditText.text.toString().trim()

        // Définition de l'action à effectuer lors du clic sur le bouton "NouvEmail"
        NouvEmailButton.setOnClickListener {
            if (email.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir le champ !", Toast.LENGTH_SHORT).show()
            } else if (!isEmailValid(email)) {
                Toast.makeText(this, "L'email en entrée est invalide !", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val nouvEmail = emailEditText.text.toString().trim()
                    val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
                    val userID = sharedPref.getInt("userID", 0)
                    val success = UserController.newUserEmail(userID, nouvEmail)

                    if (success) {
                        DatabaseManager.closeConnection()

                        val editor = sharedPref.edit()
                        editor.putBoolean("isUserLoggedIn", false)
                        editor.apply()

                        Toast.makeText(this@Activity4, "Email modifié avec succès !", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@Activity4, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Activity4,"Échec de la modification de l'email.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Définition de l'action à effectuer lors du clic sur le bouton "NouvMDP"
        NouvMDPButton.setOnClickListener {
            if (password.isEmpty() || passwordVerif.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir les champs !", Toast.LENGTH_SHORT).show()
            } else if (!isPasswordValid(password)) {
                Toast.makeText(this, "Mot de passe invalide ou trop peu sécurisé.", Toast.LENGTH_SHORT).show()
            } else if (password != passwordVerif) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas.", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val nouvPassword = UserController.encryptPassword(password)
                    val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
                    val userID = sharedPref.getInt("userID", 0)
                    val success = UserController.newUserPassword(nouvPassword, userID)

                    if (success) {
                        DatabaseManager.closeConnection()

                        val editor = sharedPref.edit()
                        editor.putBoolean("isUserLoggedIn", false)
                        editor.apply()

                        Toast.makeText(this@Activity4,"Mot de passe modifié avec succès !", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@Activity4, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Activity4, "Échec de la modification du mot de passe.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Définition de l'action à effectuer lors du clic sur le bouton "NouvNickname"
        NouvNicknameButton.setOnClickListener {
            if (nickname.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir le champ !", Toast.LENGTH_SHORT).show()
            } else if (!isNicknameValid(nickname)) {
                Toast.makeText(this, "Pseudo invalide.", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val nouvNickname = nicknameEditText.text.toString().trim()
                    val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
                    val userID = sharedPref.getInt("userID", 0)
                    val success = UserController.newUserNickname(nouvNickname, userID)

                    if (success) {
                        DatabaseManager.closeConnection()

                        val editor = sharedPref.edit()
                        editor.putBoolean("isUserLoggedIn", false)
                        editor.apply()

                        Toast.makeText(this@Activity4,"Mot de passe modifié avec succès !", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@Activity4, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Activity4, "Échec de la modification du mot de passe.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = Pattern.compile("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+\$")
        return emailPattern.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@!?_%$|\\\\]).{8,}$")
        return passwordPattern.matcher(password).matches()
    }

    private fun isNicknameValid(nickname: String): Boolean {
        // Vérifier si le pseudonyme contient uniquement des caractères alphanumériques ou des caractères spéciaux
        val nicknamePattern = Pattern.compile("^[a-zA-Z0-9!?@_-]+$")
        val isNicknameValid = nicknamePattern.matcher(nickname).matches()

        // Vérifier si le pseudonyme ne contient aucun terme inapproprié
        val containsInappropriate = inappropriateNicknames.any { inappropriateNickname ->
            nickname.contains(inappropriateNickname, ignoreCase = true)
        }

        // Vérifier si le pseudonyme ne contient que des chiffres ou est inférieur à 5 caractères
        val isNumeric = nickname.matches("[0-9]+".toRegex())
        val isLengthValid = nickname.length >= 5

        return isNicknameValid && !containsInappropriate && !isNumeric && isLengthValid
    }

}
