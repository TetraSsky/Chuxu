package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.chuxu.R
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Pattern
import kotlin.experimental.and
import com.example.chuxu.UIUtil
import com.example.chuxu.controller.UserController

class Activity3 : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVerifEditText: EditText
    private lateinit var nicknameEditText: EditText

    private val inappropriateNicknames = listOf("negro", "négro", "nègre")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inscription)

        // Activer le mode "Edge-to-Edge" pour étendre le contenu sur les bords de l'écran
        enableEdgeToEdge()
        // Masquer la barre de navigation et la barre de statut
        UIUtil.toggleSystemUI(this)

        emailEditText = findViewById(R.id.Email)
        passwordEditText = findViewById(R.id.Password)
        passwordVerifEditText = findViewById(R.id.PasswordVerif)
        nicknameEditText = findViewById(R.id.Nickname)
        val inscripButton = findViewById<Button>(R.id.Inscrip)

        inscripButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val passwordVerif = passwordVerifEditText.text.toString()
            val nickname = nicknameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || passwordVerif.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs !", Toast.LENGTH_SHORT)
                    .show()
            } else if (!isEmailValid(email)) {
                Toast.makeText(this, "L'email en entrée est invalide !", Toast.LENGTH_SHORT).show()
            } else if (!isPasswordValid(password)) {
                Toast.makeText(
                    this,
                    "Mot de passe invalide ou trop peu sécurisé.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (password != passwordVerif) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT)
                    .show()
            } else if (!isNicknameValid(nickname)) {
                Toast.makeText(
                    this,
                    "Pseudo invalide.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val encryptedPassword = encryptPassword(password)
                RegisterUserTask().execute(email, encryptedPassword, nickname)
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern =
            Pattern.compile("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+\$")
        return emailPattern.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordPattern =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@!?_%$|\\\\]).{8,}$")
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

    private fun encryptPassword(password: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
            md.update(password.toByteArray())

            val byteData = md.digest()
            val sb = StringBuilder()
            for (i in byteData.indices) {
                sb.append(
                    ((byteData[i] and 0xff.toByte()) + 0x100).toString(16).substring(1)
                )
            }
            return sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    inner class RegisterUserTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg params: String): Boolean {
            val email = params[0]
            val userPassword = params[1]
            val nickname = params[2]

            return UserController.registerUser(email, userPassword, nickname)
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                // Redirection vers la page suivante si l'utilisateur est connecté avec succès
                val intent = Intent(this@Activity3, Activity2::class.java)
                startActivity(intent)

                // Enregistrement de l'état de connexion
                val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean("isUserLoggedIn", true)
                editor.putString("userNickname", nicknameEditText.text.toString().trim())
                editor.apply()
            } else {
                Toast.makeText(applicationContext, "Erreur lors de l'inscription", Toast.LENGTH_LONG).show()
            }
        }
    }
}