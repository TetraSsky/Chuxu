package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chuxu.DatabaseManager
import com.example.chuxu.R
import com.example.chuxu.SteamAPIManager
import com.example.chuxu.UIUtil
import com.example.chuxu.controller.GameController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Activity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Activer le mode "Edge-to-Edge" pour étendre le contenu sur les bords de l'écran
        enableEdgeToEdge()
        // Masquer la barre de navigation et la barre de statut
        UIUtil.toggleSystemUI(this)

        // Ajouter une écouteur pour ajuster le padding en fonction des barres de système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupérer l'email de l'utilisateur depuis les préférences partagées
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        val userNickname = sharedPref.getString("userNickname", "")
        findViewById<TextView>(R.id.News).text = "Bienvenue $userNickname !"

        val APIButton = findViewById<Button>(R.id.API)
        APIButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                SteamAPIManager.fetchGameInfo()
            }
        }

        //Initialisation du bouton Compte
        val informationCompte = findViewById<ImageButton>(R.id.Compte)
        informationCompte.setOnClickListener {
            // Rediriger l'utilisateur vers Activity3
            val intent = Intent(this, Activity4::class.java)
            startActivity(intent)
        }

        // Ajouter un OnClickListener au bouton "Deconnect"
        val deconnectButton = findViewById<ImageButton>(R.id.Deconnect)
        deconnectButton.setOnClickListener {
            // Fermer la connexion à la base de données
            DatabaseManager.closeConnection()

            // Mettre à jour la session de l'utilisateur pour indiquer qu'il n'est plus connecté
            val editor = sharedPref.edit()
            editor.putBoolean("isUserLoggedIn", false)
            editor.apply()

            // Rediriger l'utilisateur vers l'écran de connexion (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Fonction pour vérifier si le nom du jeu répond aux critères spécifiés
    fun isGameNameValid(name: String): Boolean {
        val invalidTerms = listOf(
            "Hentai", "Sex", "Porno", "Hitler", "Staline", "Furry",
            "Futa", "Futanari", "Furries", "Porn", "+18", "Dick", "Penis", "Sexual"
        )
        // Vérifie si le nom du jeu contient des termes invalides
        for (term in invalidTerms) {
            if (name.contains(term, ignoreCase = true)) {
                return false
            }
        }
        // Vérifie si le nom du jeu contient uniquement des caractères ASCII
        if (!name.all { it.isLetterOrDigit() || it.isWhitespace() || it.isDigit() }) {
            return false
        }
        return true
    }
}