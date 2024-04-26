package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.chuxu.DatabaseManager
import com.example.chuxu.R
import com.google.android.material.navigation.NavigationView


class Activity2 : AppCompatActivity() {

    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation du bouton de recherche
        val researchButton = findViewById<Button>(R.id.Recherche)

        // Récupérer l'email de l'utilisateur depuis les préférences partagées
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        val userNickname = sharedPref.getString("userNickname", "")
        findViewById<TextView>(R.id.News).text = "Bienvenue $userNickname !"

        researchButton.setOnClickListener {
            // Rediriger l'utilisateur vers Activity5
            val intent = Intent(this, Activity5::class.java)
            startActivity(intent)
        }

        // Activer le mode "Edge-to-Edge" pour étendre le contenu sur les bords de l'écran
        enableEdgeToEdge()

        // Valeurs nécessaires à la navigation
        val drawerLayout : DrawerLayout = findViewById(R.id.MenuBurger)
        val navView : NavigationView = findViewById(R.id.nav_view)

        // Initialisation de la barre de navigation + dependances
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.Menu -> {
                }
                R.id.Compte -> {
                    // Rediriger l'utilisateur vers son compte
                    val intent = Intent(this, Activity4::class.java)
                    startActivity(intent)
                    finish()
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}