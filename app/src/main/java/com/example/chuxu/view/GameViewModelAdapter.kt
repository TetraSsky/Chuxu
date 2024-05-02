package com.example.chuxu.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.R
import com.squareup.picasso.Picasso

/**
Cette classe est un adaptateur pour le RecyclerView de "recherche.xml"
Lie les données de la liste (donc ici, une liste de GameViewModel) avec les vues dans chaque élément de la liste
- Crée les vues pour chaque élément de la liste et les remplit avec les données appropriées en utilisant les instances de GameViewModel
- Gère la création des vues (onCreateViewHolder), le remplissage (onBindViewHolder) et le calcul total d'éléments (getItemCount)
 */

class GameViewModelAdapter : RecyclerView.Adapter<GameViewModelAdapter.GameViewHolder>() {

    private var gameViewModels: List<GameViewModel> = ArrayList()
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
            leaveReviewClickListener?.onLeaveReviewClicked(currentGame)
        }

        holder.viewReviewButton.setOnClickListener {
            viewReviewsClickListener?.onViewReviewsClicked(currentGame)
        }
    }

    override fun getItemCount(): Int {
        return gameViewModels.size
    }

    fun setData(data: List<GameViewModel>) {
        this.gameViewModels = data
        notifyDataSetChanged()
    }

    fun setLeaveReviewClickListener(listener: OnLeaveReviewClickListener) {
        this.leaveReviewClickListener = listener
    }

    fun setViewReviewsClickListener(listener: OnViewReviewsClickListener) {
        this.viewReviewsClickListener = listener
    }

    interface OnLeaveReviewClickListener {
        fun onLeaveReviewClicked(gameViewModel: GameViewModel)
    }

    interface OnViewReviewsClickListener {
        fun onViewReviewsClicked(gameViewModel: GameViewModel)
    }

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameNameTextView: TextView = itemView.findViewById(R.id.gameName)
        private val gameTypeTextView: TextView = itemView.findViewById(R.id.gameType)
        private val gamePrixTextView: TextView = itemView.findViewById(R.id.gamePrix)
        private val gameDescTextView: TextView = itemView.findViewById(R.id.gameDesc)
        private val gameIdTextView: TextView = itemView.findViewById(R.id.gameId)
        private val gameImgImageView: ImageView = itemView.findViewById(R.id.gameImg)
        val leaveReviewButton: Button = itemView.findViewById(R.id.leaveReview)
        val viewReviewButton: Button = itemView.findViewById(R.id.viewReviews)

        fun bind(gameViewModel: GameViewModel) {
            gameIdTextView.text = gameViewModel.getGameIdTextView().toString()
            gameNameTextView.text = gameViewModel.getGameNameTextView()
            gameTypeTextView.text = gameViewModel.getGameTypeTextView()
            gamePrixTextView.text = gameViewModel.getGamePrixTextView()
            gameDescTextView.text = gameViewModel.getGameDescTextView()
            Picasso.get().load(gameViewModel.getGameImgImageView()).into(gameImgImageView)
        }
    }
}