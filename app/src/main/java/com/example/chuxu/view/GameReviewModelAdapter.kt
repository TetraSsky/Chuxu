package com.example.chuxu.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.R

/**
 * Adapter pour le RecyclerView des avis d'utilisateurs sur un jeu spécifique
 * Lie les données des avis avec les vues dans chaque élément de la liste
 */
class GameReviewModelAdapter : RecyclerView.Adapter<GameReviewModelAdapter.GameReviewViewHolder>() {

    private var reviews: List<GameReviewModel> = ArrayList()
    private var deleteReviewClickListener: OnDeleteReviewClickListener? = null
    private var modifyReviewClickListener: OnModifyReviewClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.constraint_review_row, parent, false)
        return GameReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameReviewViewHolder, position: Int) {
        val currentReview = reviews[position]
        holder.bind(currentReview)

        holder.deleteReviewButton.setOnClickListener {
            deleteReviewClickListener?.onLeaveReviewClicked(currentGame.gameIdTextView, currentGame.gameNameTextView)
        }

        holder.modifyReviewButton.setOnClickListener {
            modifyReviewClickListener?.onViewReviewsClicked(currentGame.gameIdTextView, currentGame.gameNameTextView)
        }
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    fun setData(data: List<GameReviewModel>) {
        this.reviews = data
        notifyDataSetChanged()
    }

    inner class GameReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameTextView: TextView = itemView.findViewById(R.id.userName)
        private val gameNameTextView: TextView = itemView.findViewById(R.id.gameName)
        private val reviewMessageTextView: TextView = itemView.findViewById(R.id.reviewMessage)
        private val reviewDateTextView: TextView = itemView.findViewById(R.id.reviewDate)
        val deleteReviewButton: ImageView = itemView.findViewById(R.id.delete)
        val modifyReviewButton: ImageView = itemView.findViewById(R.id.modify)

        fun bind(review: GameReviewModel) {
            userNameTextView.text = review.userName
            gameNameTextView.text = review.gameName
            reviewMessageTextView.text = review.reviewMessage
            reviewDateTextView.text = "Publié le : " + review.reviewDate
        }
    }
}