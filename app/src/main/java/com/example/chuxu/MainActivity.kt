package com.example.chuxu

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.sql.Connection
import java.sql.SQLException
import kotlin.experimental.and

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connexion)

        // Masquer la barre de navigation et la barre de statut
        toggleSystemUI()

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
                // Exécution de la tâche de connexion dans un thread séparé
                LoginTask().execute(email, password)
                val editor = sharedPref.edit()
                editor.putString("userEmail", email)
                editor.apply()
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

    // Fonction pour masquer la barre de navigation et la barre de statut
    private fun toggleSystemUI() {
        window.decorView.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    // Tâche asynchrone pour la connexion à la base de données
    private inner class LoginTask : AsyncTask<String, Void, Pair<Boolean, String>>() {
        override fun doInBackground(vararg params: String?): Pair<Boolean, String> {
            val email = params[0]
            val userPassword = params[1]

            var connection: Connection? = null

            try {
                // Récupérer la connexion à partir de DatabaseManager
                connection = DatabaseManager.getConnection()

                // Vérifier si la connexion est réussie
                if (connection != null) {
                    // Requête pour récupérer le mot de passe crypté de l'utilisateur
                    val query = "SELECT Password, Nickname FROM Utilisateur WHERE Email=?"
                    val statement = connection.prepareStatement(query)
                    statement.setString(1, email)
                    val resultSet = statement.executeQuery()

                    // Vérification si l'utilisateur existe dans la base de données
                    if (resultSet.next()) {
                        val storedPassword = resultSet.getString("Password")
                        val nickname = resultSet.getString("Nickname")
                        // Comparaison des mots de passe cryptés
                        val passwordMatch = storedPassword == userPassword?.let { encryptPassword(it) }
                        return Pair(passwordMatch, nickname)
                    } else {
                        return Pair(false, "")
                    }
                } else {
                    return Pair(false, "")
                }

            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return Pair(false, "")
        } // Ne pas fermer la connexion exprès

        override fun onPostExecute(result: Pair<Boolean, String>) {
            val (passwordMatch, nickname) = result
            if (passwordMatch) {
                // Redirection vers la page suivante si l'utilisateur est connecté avec succès
                val intent = Intent(this@MainActivity, Activity2::class.java)
                startActivity(intent)

                // Enregistrement de l'état de connexion et du nickname de l'utilisateur
                val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean("isUserLoggedIn", true)
                editor.putString("userNickname", nickname)
                editor.apply()
            } else {
                // Affichage d'un message d'erreur si les identifiants sont incorrects
                Toast.makeText(this@MainActivity, "Identifiants incorrects. Veuillez réessayer.", Toast.LENGTH_LONG).show()
            }
        }

        // Fonction pour crypter le mot de passe
        private fun encryptPassword(password: String): String {
            val md: MessageDigest
            try {
                md = MessageDigest.getInstance("SHA-256")
                md.update(password.toByteArray())

                val byteData = md.digest()

                // Convert the byte to hex format
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
    }
}