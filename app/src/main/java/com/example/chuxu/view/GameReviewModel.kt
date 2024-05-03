package com.example.chuxu.view

import java.time.LocalDate

/**
 * Modèle de données pour représenter un avis d'utilisateur sur un jeu spécifique.
 */
data class GameReviewModel(
    val userName: String,
    val gameName: String,
    val reviewMessage: String,
    val reviewDate: LocalDate
)
