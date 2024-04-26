package com.example.chuxu.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.R
import com.squareup.picasso.Picasso

class GameViewModelAdapter : RecyclerView.Adapter<GameViewModelAdapter.GameViewHolder>() {

    private var gameViewModels: List<GameViewModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.constraint_view_row, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val currentGame = gameViewModels[position]
        holder.bind(currentGame)
    }

    override fun getItemCount(): Int {
        return gameViewModels.size
    }

    fun setData(data: List<GameViewModel>) {
        this.gameViewModels = data
        notifyDataSetChanged()
    }

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Déclarer les vues de chaque élément de la liste ici
        private val gameNameTextView: TextView = itemView.findViewById(R.id.gameName)
        private val gameTypeTextView: TextView = itemView.findViewById(R.id.gameType)
        private val gamePrixTextView: TextView = itemView.findViewById(R.id.gamePrix)
        private val gameDescTextView: TextView = itemView.findViewById(R.id.gameDesc)
        private val gameImgImageView: ImageView = itemView.findViewById(R.id.gameImg)

        fun bind(gameViewModel: GameViewModel) {
            // Mettre à jour les vues avec les données du GameViewModel actuel
            gameNameTextView.text = gameViewModel.getGameNameTextView()
            gameTypeTextView.text = gameViewModel.getGameTypeTextView()
            gamePrixTextView.text = gameViewModel.getGamePrixTextView()
            gameDescTextView.text = gameViewModel.getGameDescTextView()
            Picasso.get().load(gameViewModel.getGameImgImageView()).into(gameImgImageView)
        }
    }
}