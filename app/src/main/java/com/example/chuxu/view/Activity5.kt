package com.example.chuxu.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.chuxu.R
import com.example.chuxu.GameData
import com.example.chuxu.SteamAPIManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Activity5 : AppCompatActivity() {

    private lateinit var gameNameTextView: TextView
    private lateinit var gameTypeTextView: TextView
    private lateinit var gamePrixTextView: TextView
    private lateinit var gameDescTextView: TextView
    private lateinit var gameImgImageView: ImageView
    private lateinit var gameViewConstraintLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recherche)

        // Initialisation des vues
        gameNameTextView = findViewById(R.id.gameName)
        gameTypeTextView = findViewById(R.id.gameType)
        gamePrixTextView = findViewById(R.id.gamePrix)
        gameDescTextView = findViewById(R.id.gameDesc)
        gameImgImageView = findViewById(R.id.gameImg)
        gameViewConstraintLayout = findViewById(R.id.gameView)

        // Activer le mode "Edge-to-Edge" pour étendre le contenu sur les bords de l'écran
        enableEdgeToEdge()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Rechercher un jeu..."

        // Définition de l'action à effectuer lors du clic sur la barre de recherche
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Executer une recherche avec l'entrée de l'utilisateur
                query?.let {
                    performSearch(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        return true
    }

    private fun performSearch(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val games = SteamAPIManager.searchGames(query)
            if (games.isNotEmpty()) {
                displayGameDetails(games.first())
            } else {
                // Si aucun jeu n'a été trouvé
                Toast.makeText(this@Activity5, "Aucun jeu trouvé", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayGameDetails(game: GameData) {
        // Définir les vues (A changer --> Utiliser un template)
        gameNameTextView.text = game.name
        gameTypeTextView.text = game.type
        gamePrixTextView.text = game.priceOverview?.price ?: "N/A"
        gameDescTextView.text = game.description
        // Pour charger l'image du jeu
        Picasso.get().load(game.headerImage).into(gameImgImageView)
    }
}