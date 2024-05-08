package com.example.chuxu.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chuxu.R

/**
 * Adapter pour le RecyclerView des avis d'utilisateurs sur un jeu spécifique
 * Lie les données des avis avec les vues dans chaque élément de la liste
 */
class GameReviewModelAdapter : RecyclerView.Adapter<GameReviewModelAdapter.GameReviewViewHolder>() {

    private var gameReviewModel: List<GameReviewModel> = ArrayList()
    private var deleteReviewClickListener: OnDeleteReviewClickListener? = null
    private var modifyReviewClickListener: OnModifyReviewClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.constraint_review_row, parent, false)
        return GameReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameReviewViewHolder, position: Int) {
        val currentReview = gameReviewModel[position]
        holder.bind(currentReview)

        holder.deleteReviewButton.setOnClickListener {
            deleteReviewClickListener?.onDeleteReviewClicked(currentReview.userID, currentReview.reviewID)
        }

        holder.modifyReviewButton.setOnClickListener {
            modifyReviewClickListener?.onModifyReviewsClicked(currentReview.userID, currentReview.reviewID)
        }
    }

    override fun getItemCount(): Int {
        return gameReviewModel.size
    }

    fun setData(data: List<GameReviewModel>) {
        this.gameReviewModel = data
        notifyDataSetChanged()
    }

    fun setDeleteReviewClickListener(listener: OnDeleteReviewClickListener) {
        this.deleteReviewClickListener = listener
    }

    fun setModifyReviewClickListener(listener: OnModifyReviewClickListener) {
        this.modifyReviewClickListener = listener
    }

    interface OnDeleteReviewClickListener {
        fun onDeleteReviewClicked(userID: Int, reviewID: Int)
    }

    interface OnModifyReviewClickListener {
        fun onModifyReviewsClicked(userID: Int, reviewID: Int)
    }

    inner class GameReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameTextView: TextView = itemView.findViewById(R.id.userName)
        private val gameNameTextView: TextView = itemView.findViewById(R.id.gameName)
        private val reviewMessageTextView: TextView = itemView.findViewById(R.id.reviewMessage)
        private val reviewDateTextView: TextView = itemView.findViewById(R.id.reviewDate)
        private val reviewIDTextView: TextView = itemView.findViewById(R.id.reviewID)
        val deleteReviewButton: ImageButton = itemView.findViewById(R.id.delete)
        val modifyReviewButton: ImageButton = itemView.findViewById(R.id.modify)

        fun bind(gameReviewModel: GameReviewModel) {
            userNameTextView.text = gameReviewModel.userName
            gameNameTextView.text = gameReviewModel.gameName
            reviewMessageTextView.text = gameReviewModel.reviewMessage
            reviewDateTextView.text = "Publié le : " + gameReviewModel.reviewDate
            reviewIDTextView.text = gameReviewModel.reviewID.toString()
        }
    }
}