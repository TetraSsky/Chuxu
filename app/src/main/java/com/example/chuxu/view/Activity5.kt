package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.DatabaseManager
import com.example.chuxu.R
import com.example.chuxu.SteamAPIManager
import com.example.chuxu.util.SortOption
import com.example.chuxu.util.UpdateUtil
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.net.Uri
import android.view.animation.AnimationUtils
import android.widget.ImageView
import kotlinx.coroutines.Job
import java.util.Random

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
    private lateinit var progressBar: ProgressBar
    private lateinit var videoView: VideoView
    private var mediaPlayer: MediaPlayer? = null
    private var MediaPlayer: MediaPlayer? = null
    private var search: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recherche)

        rootView = findViewById(R.id.root_layout)
        defaultview = layoutInflater.inflate(R.layout.default_research_screen, null)
        rootView.addView(defaultview)

        val easterEggImageView: ImageView = defaultview.findViewById(R.id.EasterEgg)

        easterEggImageView.setOnClickListener {
            val random = Random()
            val randomValue = random.nextInt(50)

            val spinAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce)
            easterEggImageView.startAnimation(spinAnimation)

            val soundResource = when {
                randomValue == 1 -> {
                    MediaPlayer?.setVolume(0.1f, 0.1f)
                    R.raw.ibtb
                } else -> {
                    MediaPlayer?.setVolume(0.3f, 0.3f)
                    when (random.nextInt(3)) {
                        0 -> R.raw.qf
                        1 -> R.raw.qf2
                        else -> R.raw.qf3
                    }
                }
            }

            MediaPlayer?.setVolume(0.6f, 0.6f)

            mediaPlayer = android.media.MediaPlayer.create(this, soundResource)
            mediaPlayer?.start()

            mediaPlayer?.setOnCompletionListener { player ->
                player.release()
            }
        }

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

        videoView = findViewById(R.id.loadingVideoView)
        videoView.visibility = View.INVISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.Menu -> {
                    val intent = Intent(this, Activity2::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
                R.id.Compte -> {
                    val intent = Intent(this, Activity4::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
                R.id.Avis -> {
                    val intent = Intent(this, Activity8::class.java)
                    startActivity(intent)
                }
                R.id.Deconnect -> {
                    DatabaseManager.closeConnection()

                    val editor = sharedPref.edit()
                    editor.putBoolean("isUserLoggedIn", false)
                    editor.apply()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
                R.id.Update -> {
                    UpdateUtil.checkForUpdate(this)
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Rechercher un jeu..."

        val filterSearchItem = menu.findItem(R.id.action_filter_search)
        val filterSearchView = filterSearchItem.actionView as SearchView
        filterSearchView.queryHint = "Rechercher dans les résultats..."

        searchView.setOnSearchClickListener {
            if (!filterSearchView.isIconified) {
                filterSearchView.onActionViewCollapsed()
            }
        }

        filterSearchView.setOnSearchClickListener {
            if (!searchView.isIconified) {
                searchView.onActionViewCollapsed()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    val trimmedQuery = query.trim()
                    search?.cancel()
                    search = CoroutineScope(Dispatchers.Main).launch {
                        showLoadingView("Recherche en cours...", "Veuillez patienter...", trimmedQuery)

                        val videoView = findViewById<VideoView>(R.id.loadingVideoView)
                        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.backgroundanim)
                        videoView.setVideoURI(videoUri)
                        videoView.setOnPreparedListener { mp ->
                            mp.isLooping = true
                        }
                        videoView.visibility = View.VISIBLE
                        videoView.start()

                        progressBar = findViewById(R.id.progressBar2)
                        progressBar.progress = 0

                        val games = SteamAPIManager.searchGames(trimmedQuery, progressBar)
                        val gameViewModels = ArrayList<GameViewModel>()
                        for (game in games) {
                            gameViewModels.add(
                                GameViewModel(
                                    game.appId,
                                    game.name,
                                    game.type,
                                    game.priceOverview?.price ?: "N/A",
                                    game.description,
                                    game.headerImage
                                )
                            )
                        }
                        hideLoadingView()

                        videoView.stopPlayback()
                        videoView.visibility = View.GONE

                        adapter.setData(gameViewModels)
                    }
                } else {
                    Toast.makeText(this@Activity5, "Veuillez entrer quelque chose dans la barre de recherche", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        filterSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    adapter.searchWithinResults(query.trim())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.sort_name_asc -> adapter.sortData(SortOption.NAME_ASC)
            R.id.sort_name_desc -> adapter.sortData(SortOption.NAME_DESC)
            R.id.sort_price_asc -> adapter.sortData(SortOption.PRICE_ASC)
            R.id.sort_price_desc -> adapter.sortData(SortOption.PRICE_DESC)
            R.id.sort_type_game -> adapter.sortData(SortOption.GAME)
            R.id.sort_type_dlc -> adapter.sortData(SortOption.DLC)
            R.id.sort_type_demo -> adapter.sortData(SortOption.DEMO)
            R.id.sort_type_music -> adapter.sortData(SortOption.MUSIC)
            R.id.reset_filter -> adapter.sortData(SortOption.RESET)
            R.id.action_filter_search -> {
                val filterSearchView = item.actionView as SearchView
                filterSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (!query.isNullOrBlank()) {
                            adapter.searchWithinResults(query.trim())
                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return true
                    }
                })
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onLeaveReviewClicked(appId: Int, appName: String) {
        val intent = Intent(this, Activity6::class.java)
        intent.putExtra("appId", appId)
        intent.putExtra("appName", appName)
        startActivity(intent)
    }

    override fun onViewReviewsClicked(appId: Int, appName: String) {
        val intent = Intent(this, Activity7::class.java)
        intent.putExtra("appId", appId)
        startActivity(intent)
    }

    private fun showLoadingView(text1: String, text2: String, trimmedQuery: String) {
        if (!isShowingLoadingView) {
            rootView.removeView(defaultview)
            isShowingLoadingView = true
            loadingView = layoutInflater.inflate(R.layout.research_loading_screen, null)
            loadingTextView1 = loadingView.findViewById(R.id.loadingtextView1)
            loadingTextView2 = loadingView.findViewById(R.id.loadingtextView2)
            loadingTextView1.text = text1
            loadingTextView2.text = text2
            rootView.addView(loadingView)

            CoroutineScope(Dispatchers.Main).launch {
                while (isShowingLoadingView) {
                    delay(82000)
                    val messages = listOf(
                        Pair("Cela va prendre un moment...", "Conseil : Prenez un café ☕ !"),
                        Pair("Allô ? Y'a-t-il quelqu'un ?", "Si oui, patientez sâgement !"),
                        Pair("01000011 01101000 01100001 01110010 01100111 01100101 01101101 01100101 01101110 01110100", "(Ça veut dire \"Chargement\" en binaire)"),
                        Pair("\"Chúxù\" en Chinois Traditionnel (儲蓄)", "Veut dire \"Économies\" en Français !"),
                        Pair("Vous attendez ?", "Super, car nous aussi !"),
                        Pair("(╯°□°)╯︵ ┻━┻", "POURQUOI C'EST SI LONG ?!"),
                        Pair("Charger ou ne pas charger ?", "Telle est la question..."),
                        Pair("", "OÙ EST LE TEXTE DU HAUT ?!"),
                        Pair("OÙ EST LE TEXTE DU BAS ?!", ""),
                        Pair("Laissez-moi deviner...", "Vous avez recherché \"$trimmedQuery\" ?"),
                        Pair("Rappel : Ça charge encore", "Au moins maintenant, vous savez !"),
                        Pair("Pour être franc, mon créateur code un peu mal...", "(Veuillez l'en excuser \uD83D\uDE4F)"),
                        Pair("Si vous êtes en H+ avec votre SIM", "Nous espérons au moins que vous aimez cet écran..."),
                        Pair("\uD83E\uDD38\uD83D\uDCA5\uD83E\uDDBD\uD83C\uDFCC\uFE0F", "Hole in one !"),
                        Pair("Toujours pas fini ?!", "Eh oui ! Et nous en sommes désolés \uD83D\uDE4F"),
                        Pair("Qu'est-ce qui avance, est blanc et ne s'arrête pas ?", "(Réponse : Cet écran de chargement)"),
                        Pair("Recherche en cours...", "Veuillez patienter..."),
                        Pair("Connexion en co-", "OUPS ! Mauvais message \uD83D\uDE05"),
                        Pair("Vous voulez une blague ?", "Je n'ai pas fini de charger !\nVoilà..."),
                        Pair("Une envie pressante ?", "Lance un jet de sauvegarde."),
                        Pair("\uD83C\uDF0E\uD83D\uDD25","POUR LA SUPER TERRE !"),
                    )
                    val loadingMessage = messages.random()
                    loadingTextView1.text = loadingMessage.first
                    loadingTextView2.text = loadingMessage.second
                }
            }
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
}