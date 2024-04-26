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

        // Get the search menu item
        val searchItem = menu.findItem(R.id.action_search)

        // Get the search widget from the menu item
        val searchView = searchItem.actionView as SearchView

        // Set the search hint text in the search widget
        searchView.queryHint = "Rechercher un jeu..."

        // Set a text listener for the search widget
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Execute search when user submits a search query
                query?.let {
                    performSearch(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Action to perform when search text changes
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
                // Handle case when no game found
                Toast.makeText(this@Activity5, "Aucun jeu trouvé", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayGameDetails(game: GameData) {
        // Set game details to views
        gameNameTextView.text = game.name
        gameTypeTextView.text = game.type
        gamePrixTextView.text = game.priceOverview?.price ?: "N/A"
        gameDescTextView.text = game.description

        // Load game image using Picasso library
        Picasso.get().load(game.headerImage).into(gameImgImageView)
    }
}