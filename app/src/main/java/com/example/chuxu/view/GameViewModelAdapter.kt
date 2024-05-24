package com.example.chuxu.view

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.R
import com.example.chuxu.util.SortOption
import com.squareup.picasso.Picasso

/**
 * Cette classe est un adaptateur pour le RecyclerView de "recherche.xml"
 * Lie les données de la liste (ici, une liste de GameViewModel) avec les vues dans chaque élément de la liste
 * - Crée les vues pour chaque élément de la liste et les remplit avec les données appropriées en utilisant les instances de GameViewModel
 * - Gère la création des vues (onCreateViewHolder), le remplissage (onBindViewHolder) et le calcul total d'éléments (getItemCount)
 * - Fournit des fonctions pour trier les données par différents critères (nom, prix, type)
 * - Convertit les prix en double pour permettre un tri numérique correct
 * - Permet de filtrer les données par type (jeu, DLC, démo, musique) et de réinitialiser les filtres
 */
class GameViewModelAdapter : RecyclerView.Adapter<GameViewModelAdapter.GameViewHolder>() {

    private var gameViewModels: List<GameViewModel> = ArrayList()
    private var originalGameViewModels: List<GameViewModel> = ArrayList()
    private var leaveReviewClickListener: OnLeaveReviewClickListener? = null
    private var viewReviewsClickListener: OnViewReviewsClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.constraint_view_row, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val currentGame = gameViewModels[position]
        holder.bind(currentGame)

        holder.leaveReviewButton.setOnClickListener {
            leaveReviewClickListener?.onLeaveReviewClicked(currentGame.getGameIdTextView(), currentGame.getGameNameTextView())
        }

        holder.viewReviewButton.setOnClickListener {
            viewReviewsClickListener?.onViewReviewsClicked(currentGame.getGameIdTextView(), currentGame.getGameNameTextView())
        }
    }

    override fun getItemCount(): Int {
        return gameViewModels.size
    }

    fun setData(data: List<GameViewModel>) {
        this.gameViewModels = data
        this.originalGameViewModels = data.toList()
        notifyDataSetChanged()
    }

    fun setLeaveReviewClickListener(listener: OnLeaveReviewClickListener) {
        this.leaveReviewClickListener = listener
    }

    fun setViewReviewsClickListener(listener: OnViewReviewsClickListener) {
        this.viewReviewsClickListener = listener
    }

    fun sortData(sortOption: SortOption) {
        gameViewModels = when (sortOption) {
            SortOption.NAME_ASC -> gameViewModels.sortedBy { it.getGameNameTextView() }
            SortOption.NAME_DESC -> gameViewModels.sortedByDescending { it.getGameNameTextView() }
            SortOption.PRICE_ASC -> gameViewModels.filter { it.getGamePrixTextView() != "N/A" }
                .sortedBy { parsePrice(it.getGamePrixTextView()) } +
                    gameViewModels.filter { it.getGamePrixTextView() == "N/A" }
            SortOption.PRICE_DESC -> gameViewModels.filter { it.getGamePrixTextView() != "N/A" }
                .sortedByDescending { parsePrice(it.getGamePrixTextView()) } +
                    gameViewModels.filter { it.getGamePrixTextView() == "N/A" }
            SortOption.TYPE -> gameViewModels.sortedBy { it.getGameTypeTextView() }
            SortOption.GAME -> originalGameViewModels.filter { it.getGameTypeTextView() == "game" }
            SortOption.DLC -> originalGameViewModels.filter { it.getGameTypeTextView() == "dlc" }
            SortOption.DEMO -> originalGameViewModels.filter { it.getGameTypeTextView() == "demo" }
            SortOption.MUSIC -> originalGameViewModels.filter { it.getGameTypeTextView() == "music" }
            SortOption.RESET -> originalGameViewModels.toList()
        }
        notifyDataSetChanged()
    }

    private fun parsePrice(priceString: String): Double {
        val cleanedString = priceString.replace("[^\\d,\\.]".toRegex(), "")
        val normalizedString = cleanedString.replace(",", ".")
        return normalizedString.toDoubleOrNull() ?: Double.MAX_VALUE
    }

    fun searchWithinResults(query: String) {
        gameViewModels = originalGameViewModels.filter {
            it.getGameNameTextView().contains(query, ignoreCase = true)
        }
        notifyDataSetChanged()
    }

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameNameTextView: TextView = itemView.findViewById(R.id.gameName)
        private val gameTypeTextView: TextView = itemView.findViewById(R.id.gameType)
        private val gamePrixTextView: TextView = itemView.findViewById(R.id.gamePrix)
        private val gameDescTextView: TextView = itemView.findViewById(R.id.gameDesc)
        private val gameIdTextView: TextView = itemView.findViewById(R.id.gameId)
        private val gameImgImageView: ImageButton = itemView.findViewById(R.id.gameImg)
        val leaveReviewButton: Button = itemView.findViewById(R.id.leaveReview)
        val viewReviewButton: Button = itemView.findViewById(R.id.viewReviews)
        init {
            gameImgImageView.setOnClickListener {
                val context = itemView.context
                val bounceAnimation = AnimationUtils.loadAnimation(context, R.anim.bounce)
                gameImgImageView.startAnimation(bounceAnimation)

                val steamAppUrl = "https://store.steampowered.com/app/${gameIdTextView.text}/"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(steamAppUrl))
                itemView.context.startActivity(intent)
            }
        }

        fun bind(gameViewModel: GameViewModel) {
            gameIdTextView.text = gameViewModel.getGameIdTextView().toString()
            gameNameTextView.text = gameViewModel.getGameNameTextView()
            gameTypeTextView.text = gameViewModel.getGameTypeTextView()
            gamePrixTextView.text = gameViewModel.getGamePrixTextView()
            gameDescTextView.text = gameViewModel.getGameDescTextView()
            Picasso.get()
                .load(gameViewModel.getGameImgImageView())
                .fit()
                .centerCrop()
                .into(gameImgImageView)
        }
    }

    interface OnLeaveReviewClickListener {
        fun onLeaveReviewClicked(appId: Int, appName: String)
    }

    interface OnViewReviewsClickListener {
        fun onViewReviewsClicked(appId: Int, appName: String)
    }
}