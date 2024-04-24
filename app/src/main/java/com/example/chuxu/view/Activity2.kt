package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.chuxu.DatabaseManager
import com.example.chuxu.R
import com.example.chuxu.SteamAPIManager
import com.example.chuxu.UIUtil
import com.example.chuxu.controller.GameController
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Activity2 : AppCompatActivity() {

    private lateinit var toggle : ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Valeurs nécessaires à la page
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        val drawerLayout : DrawerLayout = findViewById(R.id.MenuBurger)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        /*
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
         */

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.Menu -> {
                    // Rediriger l'utilisateur vers l'acceuil
                    val intent = Intent(this, Activity2::class.java)
                    startActivity(intent)
                    //RAJOUTER VERIFICATION SI L'UTILISATEUR EST DEJA SUR CETTE PAGE AVANT DE REDIRIGER MAIS AUSSI FERMER L'ANCIENNE SUR LAQUELLE IL ETAITd
                }
                R.id.Compte -> {
                    // Rediriger l'utilisateur vers le compte
                    val intent = Intent(this, Activity4::class.java)
                    startActivity(intent)
                }
                R.id.Deconnect -> {
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
            true
        }

        val APIButton = findViewById<Button>(R.id.API)
        APIButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                SteamAPIManager.fetchGameInfo()
            }
        }

        // Récupérer l'email de l'utilisateur depuis les préférences partagées
        val userNickname = sharedPref.getString("userNickname", "")
        findViewById<TextView>(R.id.News).text = "Bienvenue $userNickname !"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}