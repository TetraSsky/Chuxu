package com.example.chuxu.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.chuxu.R
import com.example.chuxu.controller.UserController
import kotlinx.coroutines.*
import androidx.appcompat.widget.SearchView

class Activity5 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recherche)

        // Activer le mode "Edge-to-Edge" pour étendre le contenu sur les bords de l'écran
        enableEdgeToEdge()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        // Récupérer l'item du menu de recherche
        val searchItem = menu.findItem(R.id.action_search)

        // Récupérer le widget de recherche à partir de l'item du menu
        val searchView = searchItem.actionView as SearchView

        // Définir le texte de l'invite dans le widget de recherche
        searchView.queryHint = "Taper ici pour rechercher"

        // Définir un écouteur de texte pour le widget de recherche
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Action à effectuer lorsque l'utilisateur soumet une requête de recherche
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Action à effectuer lorsque le texte de recherche change
                return true
            }
        })

        return true
    }

    /*
    Créer fonction pour :
    - Chercher tous les jeux de la BDD et pré-scrap la description, une image, le prix et le type
    - Les afficher dans un scrollview selon ce qui a été tapé dans la barre de recherche
    - Afficher aussi les résultats contenant les mots de la recherche ( du + )
    - Rajouter des boutons sur le côté pour le tri (Prix croissant/décroissant, etc...)
    -
     */
}