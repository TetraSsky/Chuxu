package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.DatabaseManager
import com.example.chuxu.R
import com.example.chuxu.GameData
import com.example.chuxu.SteamAPIManager
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Activity5 : AppCompatActivity() {

    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GameViewModelAdapter
    private lateinit var loadingView: View
    private lateinit var longloadingView: View
    private lateinit var defaultview: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recherche)

        // Initialiser le RecyclerView et l'Adapter
        recyclerView = findViewById(R.id.myRecyclerView)
        adapter = GameViewModelAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Valeurs nécessaires à la navigation
        val drawerLayout : DrawerLayout = findViewById(R.id.MenuBurger)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)

        // Afficher par défault pour ne pas faire vide
        defaultview = layoutInflater.inflate(R.layout.default_research_screen, findViewById(android.R.id.content), false)
        (findViewById<ViewGroup>(android.R.id.content)).addView(defaultview)

        // Activer le mode "Edge-to-Edge" pour étendre le contenu sur les bords de l'écran
        enableEdgeToEdge()

        // Initialisation de la barre de navigation + dependances
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.Menu -> {
                    // Rediriger l'utilisateur vers l'acceuil
                    val intent = Intent(this, Activity2::class.java)
                    startActivity(intent)
                    finish()
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

    // Fonction pour toggle le menu burger (Ouverture/Fermeture)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Fonction pour inflate l'ActionBar avec celle de recherche
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Rechercher un jeu..."

        // Définition de l'action à effectuer lors du clic sur la barre de recherche
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    performSearch(query)
                } else {
                    Toast.makeText(this@Activity5, "Veuillez entrer quelque chose dans la barre de recherche", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        return true
    }

    private fun showLoadingView() {
        loadingView = layoutInflater.inflate(R.layout.loading_screen, findViewById(android.R.id.content), false)
        (findViewById<ViewGroup>(android.R.id.content)).addView(loadingView)
    }

    private fun hideLoadingView() {
        (findViewById<ViewGroup>(android.R.id.content)).removeView(loadingView)
    }

    private fun showLongLoadingView() {
        longloadingView = layoutInflater.inflate(R.layout.long_loading_screen, findViewById(android.R.id.content), false)
        (findViewById<ViewGroup>(android.R.id.content)).addView(longloadingView)
    }

    private fun hideLongLoadingView() {
        (findViewById<ViewGroup>(android.R.id.content)).removeView(longloadingView)
    }

    private fun destroyDefaultView() {
        (findViewById<ViewGroup>(android.R.id.content)).removeView(defaultview)
    }

    private fun performSearch(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            destroyDefaultView()
            val startTime = System.currentTimeMillis()
            showLoadingView()
            val games = SteamAPIManager.searchGames(query)
            val elapsedTime = System.currentTimeMillis() - startTime
            val gameViewModels = ArrayList<GameViewModel>()
            for (game in games) {
                gameViewModels.add(
                    GameViewModel(
                        game.name,
                        game.type,
                        game.priceOverview?.price ?: "N/A",
                        game.description,
                        game.headerImage
                    )
                )
            }
            if (elapsedTime >= 60000) { // Si le temps écoulé est supérieur ou égal à une minute, afficher chargement long
                showLongLoadingView()
            } else {
                hideLongLoadingView()
            }
            hideLongLoadingView()
            hideLoadingView()
            adapter.setData(gameViewModels)
        }
    }
}