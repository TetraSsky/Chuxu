package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.DatabaseManager
import com.example.chuxu.R
import com.example.chuxu.SteamAPIManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activité tierciaire de l'application, permet à l'utilisateur d'effectuer des recherches en lien avec l'API de Steam
 */
class Activity5 : AppCompatActivity(), GameViewModelAdapter.OnLeaveReviewClickListener, GameViewModelAdapter.OnViewReviewsClickListener {

    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GameViewModelAdapter
    private var isShowingLoadingView = false
    private lateinit var loadingView: View
    private lateinit var loadingTextView1: TextView
    private lateinit var loadingTextView2: TextView
    private lateinit var defaultview: View
    private lateinit var rootView: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recherche)

        rootView = findViewById(R.id.root_layout)
        defaultview = layoutInflater.inflate(R.layout.default_research_screen, null)
        rootView.addView(defaultview)

        recyclerView = findViewById(R.id.myRecyclerView)
        adapter = GameViewModelAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter.setLeaveReviewClickListener(this)
        adapter.setViewReviewsClickListener(this)
        val drawerLayout : DrawerLayout = findViewById(R.id.MenuBurger)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val sharedPref = getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.Menu -> {
                    val intent = Intent(this, Activity2::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.Compte -> {
                    val intent = Intent(this, Activity4::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.Deconnect -> {
                    DatabaseManager.closeConnection()

                    val editor = sharedPref.edit()
                    editor.putBoolean("isUserLoggedIn", false)
                    editor.apply()

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Rechercher un jeu..."

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

    private fun showLoadingView(text1: String, text2: String) {
        if (!isShowingLoadingView) {
            rootView.removeView(defaultview)
            isShowingLoadingView = true
            loadingView = layoutInflater.inflate(R.layout.loading_screen, null)
            loadingTextView1 = loadingView.findViewById(R.id.loadingtextView1)
            loadingTextView2 = loadingView.findViewById(R.id.loadingtextView2)
            loadingTextView1.text = text1
            loadingTextView2.text = text2
            rootView.addView(loadingView)
        } else {
            updateLoadingView(text1, text2)
        }
    }

    private fun updateLoadingView(text1: String, text2: String) {
        loadingTextView1.text = text1
        loadingTextView2.text = text2
    }

    private fun hideLoadingView() {
        isShowingLoadingView = false
        rootView.removeView(loadingView)
    }

    private fun performSearch(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            showLoadingView("Recherche en cours...", "Veuillez patienter...")
            val games = SteamAPIManager.searchGames(query)
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
            hideLoadingView()
            adapter.setData(gameViewModels)
        }
    }

    override fun onLeaveReviewClicked(gameViewModel: GameViewModel) {
        val intent = Intent(this, Activity6::class.java)
        startActivity(intent)
    }

    override fun onViewReviewsClicked(gameViewModel: GameViewModel) {

    }
}