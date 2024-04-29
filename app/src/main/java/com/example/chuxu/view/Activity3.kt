package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.chuxu.DatabaseManager
import com.example.chuxu.R
import java.util.regex.Pattern
import com.example.chuxu.controller.UserController
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Activity3 : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVerifEditText: EditText
    private lateinit var nicknameEditText: EditText
    private val inappropriateNicknames = listOf(
        // Français
        "negro", "négro", "nègre", "BlancSuprémaciste", "Raciste", "KKK", "Esclavage", "Aryan", "Nazi", "Hitler", "Fasciste", "Colonialiste", "Ségrégation", "SuprématieBlanche",
        "Violence", "Haine", "Terroriste", "Pornographie", "Porno", "Automutilation", "Suicide",
        "Trump", "Biden", "Macron", "Poutine", "XiJinping", "Erdogan", "KimJongUn", "Assad", "Netanyahu", "Merkel", "Laden", "Al-Qaïda",

        // Anglais
        "WhiteSupremacist", "Racist", "Slavery", "Aryan", "Nazi", "Hitler", "Fascist", "Colonialist", "Segregation", "WhiteSupremacy",
        "Violence", "Hate", "Terrorist", "Pornography", "SelfHarm", "Sex", "Fuck",

        // Espagnol
        "BlancoSupremacista", "Racista", "Esclavitud", "Nazi", "Hitler", "Fascista", "Colonialista", "Segregación",
        "Terrorista", "Pornografía", "Automutilación", "Suicidio",
    )
    private var isShowingLoadingView = false
    private lateinit var loadingView: View
    private lateinit var loadingTextView1: TextView
    private lateinit var loadingTextView2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inscription)

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
                Toast.makeText(this, "Veuillez remplir tous les champs !", Toast.LENGTH_SHORT).show()
            } else if (!isEmailValid(email)) {
                Toast.makeText(this, "L'email en entrée est invalide !", Toast.LENGTH_SHORT).show()
            } else if (!isPasswordValid(password)) {
                Toast.makeText(this,"Mot de passe invalide ou trop peu sécurisé.", Toast.LENGTH_SHORT).show()
            } else if (password != passwordVerif) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas.", Toast.LENGTH_SHORT).show()
            } else if (!isNicknameValid(nickname)) {
                Toast.makeText(this, "Pseudo invalide.", Toast.LENGTH_SHORT).show()
            } else {
                showLoadingView("Connexion...", "Veuillez patienter...")
                val encryptedPassword = UserController.encryptPassword(password)
                RegisterUserTask().execute(email, encryptedPassword, nickname)
            }
        }

        // Activer le mode "Edge-to-Edge" pour étendre le contenu sur les bords de l'écran
        enableEdgeToEdge()

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

    inner class RegisterUserTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg params: String): Boolean {
            val email = params[0]
            val userPassword = params[1]
            val nickname = params[2]

            return UserController.registerUser(email, userPassword, nickname)
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                CoroutineScope(Dispatchers.Main).launch {
                    val email = emailEditText.text.toString().trim()
                    val userID = withContext(Dispatchers.IO) {
                        UserController.getUserID(email)
                    }

                    if (userID != 0) {
                        // Redirection vers la page suivante si l'utilisateur est connecté avec succès
                        val intent = Intent(this@Activity3, Activity2::class.java)
                        startActivity(intent)
                        finish()

                        hideLoadingView()

                        // Enregistrement de l'état de connexion
                        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putBoolean("isUserLoggedIn", true)
                        editor.putString("userEmail", email)
                        editor.putString("userNickname", nicknameEditText.text.toString().trim())
                        editor.putInt("userID", userID)
                        editor.apply()
                    } else {
                        hideLoadingView()
                        Toast.makeText(this@Activity3, "Une erreur est survenue.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                hideLoadingView()
                Toast.makeText(applicationContext, "Erreur lors de l'inscription", Toast.LENGTH_LONG).show()
            }
        }
    }

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